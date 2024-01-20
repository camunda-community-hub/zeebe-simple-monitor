package io.zeebe.monitor.zeebe.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.ElasticConfig;
import io.zeebe.monitor.repository.ElasticConfigRepository;
import io.zeebe.monitor.zeebe.hazelcast.importers.ErrorHazelcastImporter;
import io.zeebe.monitor.zeebe.hazelcast.importers.IncidentHazelcastImporter;
import io.zeebe.monitor.zeebe.hazelcast.importers.JobHazelcastImporter;
import io.zeebe.monitor.zeebe.hazelcast.importers.MessageHazelcastImporter;
import io.zeebe.monitor.zeebe.hazelcast.importers.MessageSubscriptionHazelcastImporter;
import io.zeebe.monitor.zeebe.hazelcast.importers.ProcessAndElementHazelcastImporter;
import io.zeebe.monitor.zeebe.hazelcast.importers.TimerHazelcastImporter;
import io.zeebe.monitor.zeebe.hazelcast.importers.VariableHazelcastImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ConditionalOnProperty(value="mode", havingValue = "elastic")
public class ZeebeElasticImportService {
    private static final Logger LOG = LoggerFactory.getLogger(ZeebeElasticImportService.class);

    @Value("${elastic.index}")
    private String elasticIndex;

    @Autowired
    private ProcessAndElementHazelcastImporter processAndElementImporter;
    @Autowired
    private VariableHazelcastImporter variableImporter;
    @Autowired
    private JobHazelcastImporter jobImporter;
    @Autowired
    private IncidentHazelcastImporter incidentImporter;
    @Autowired
    private MessageHazelcastImporter messageImporter;
    @Autowired
    private MessageSubscriptionHazelcastImporter messageSubscriptionImporter;
    @Autowired
    private TimerHazelcastImporter timerImporter;
    @Autowired
    private ErrorHazelcastImporter errorImporter;

    @Autowired
    private ElasticConfigRepository elasticConfigRepository;

    public void importFrom(final ElasticsearchClient elastic) throws Exception {

        final var elasticConfig =
                elasticConfigRepository
                        .findById("cfg")
                        .orElseGet(
                                () -> {
                                    final var config = new ElasticConfig();
                                    config.setId("cfg");
                                    config.setTimestamp(-1);
                                    return config;
                                });
        LOG.info("Start importing from {}", ZonedDateTime.ofInstant(Instant.ofEpochMilli(elasticConfig.getTimestamp()), ZoneId.systemDefault()));

        try {

            SearchResponse<ObjectNode> data = elastic.search(
                    getRequest(elasticIndex, elasticConfig.getTimestamp()),
                    ObjectNode.class);

            AtomicInteger counter = new AtomicInteger(0);
            data.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .map(r -> importRecord(r, counter))
                    .reduce(Long::max)
                    .ifPresent(t -> {
                        LOG.info("Imported {} records from elastic till {}", counter.get(), ZonedDateTime.ofInstant(Instant.ofEpochMilli(t), ZoneId.systemDefault()));
                        elasticConfig.setTimestamp(t);
                        elasticConfigRepository.save(elasticConfig);
                    });
        } catch (Exception e) {
            LOG.error("Error while importing.", e);
        }

    }

    private SearchRequest getRequest(String index, long timestamp) {
        return new SearchRequest.Builder().index(index)
                .query(q -> q.range(
                        rb -> rb.field("timestamp").gt(JsonData.of(timestamp))
                )).sort(s -> s.field(f -> f.field("timestamp")))
                .size(1000).build();
    }

    private long importRecord(ObjectNode record, AtomicInteger counter) {
        String type = record.get("valueType").asText();


        switch (type) {
            case "PROCESS" -> {
                var builder = Schema.ProcessRecord.newBuilder();
                parseJsonToProto(record, builder);
                processAndElementImporter.importProcess(builder.build());
            }
            case "PROCESS_INSTANCE" -> {
                var builder = Schema.ProcessInstanceRecord.newBuilder();
                parseJsonToProto(record, builder);
                processAndElementImporter.importProcessInstance(builder.build());
            }
            case "INCIDENT" -> {
                var builder = Schema.IncidentRecord.newBuilder();
                parseJsonToProto(record, builder);
                incidentImporter.importIncident(builder.build());
            }
            case "JOB" -> {
                var builder = Schema.JobRecord.newBuilder();
                parseJsonToProto(record, builder);
                jobImporter.importJob(builder.build());
            }
            case "VARIABLE" -> {
                var builder = Schema.VariableRecord.newBuilder();
                parseJsonToProto(record, builder);
                variableImporter.importVariable(builder.build());
            }
            case "TIMER" -> {
                var builder = Schema.TimerRecord.newBuilder();
                parseJsonToProto(record, builder);
                timerImporter.importTimer(builder.build());
            }
            case "MESSAGE" -> {
                var builder = Schema.MessageRecord.newBuilder();
                parseJsonToProto(record, builder);
                messageImporter.importMessage(builder.build());
            }
            case "MESSAGE_SUBSCRIPTION", "PROCESS_MESSAGE_SUBSCRIPTION" -> {
                var builder = Schema.MessageSubscriptionRecord.newBuilder();
                parseJsonToProto(record, builder);
                messageSubscriptionImporter.importMessageSubscription(builder.build());
            }
            case "MESSAGE_START_EVENT_SUBSCRIPTION" -> {
                var builder = Schema.MessageStartEventSubscriptionRecord.newBuilder();
                parseJsonToProto(record, builder);
                messageSubscriptionImporter.importMessageStartEventSubscription(builder.build());
            }
            case "ERROR" -> {
                var builder = Schema.ErrorRecord.newBuilder();
                parseJsonToProto(record, builder);
                errorImporter.importError(builder.build());
            }
            default -> LOG.error("Unknown type {}", type);
        }

        counter.incrementAndGet();
        return record.get("timestamp").asLong();


    }

    private void parseJsonToProto(ObjectNode record, Message.Builder builder) {
        var metadata = record.deepCopy();
        metadata.remove("value");
        var value = record.with("value");
        value.set("metadata", metadata);

        try {
            JsonFormat.parser().ignoringUnknownFields().merge(value.toString(), builder);
        } catch (InvalidProtocolBufferException ex) {
            throw new RuntimeException("Error on converting json to protobuf", ex);
        }
    }
}
