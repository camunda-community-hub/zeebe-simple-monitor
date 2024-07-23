package io.zeebe.monitor.zeebe;

import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.rest.dto.GenericKafkaRecord;
import io.zeebe.monitor.zeebe.importers.ErrorImporter;
import io.zeebe.monitor.zeebe.importers.IncidentImporter;
import io.zeebe.monitor.zeebe.importers.JobImporter;
import io.zeebe.monitor.zeebe.importers.MessageImporter;
import io.zeebe.monitor.zeebe.importers.MessageSubscriptionImporter;
import io.zeebe.monitor.zeebe.importers.ProcessAndElementImporter;
import io.zeebe.monitor.zeebe.importers.TimerImporter;
import io.zeebe.monitor.zeebe.importers.VariableImporter;
import io.zeebe.monitor.zeebe.util.BuildRecordUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.zeebe.monitor.zeebe.util.ImportUtil.ifEvent;

import org.apache.kafka.streams.kstream.KStream;

@Profile("kafka")
@Component
public class KafkaStreamsService {
    @Value("${numOfImportThreads:5}")
    public int numOfThreads;

    @Autowired
    private ProcessAndElementImporter processAndElementImporter;

    @Autowired
    private TimerImporter timerImporter;

    @Autowired
    private VariableImporter variableImporter;

    @Autowired
    private ErrorImporter errorImporter;

    @Autowired
    private MessageImporter messageImporter;

    @Autowired
    private IncidentImporter incidentImporter;

    @Autowired
    private MessageSubscriptionImporter messageSubscriptionImporter;

    @Autowired
    private JobImporter jobImporter;

    @Autowired
    private ZeebeKafkaStreams zeebeKafkaStreams;

    private ExecutorService executorService;

    @PostConstruct
    public void start() {
        executorService = Executors.newFixedThreadPool(numOfThreads);

        processStream(zeebeKafkaStreams.kafkaProcessInstanceStream(),
                processAndElementImporter::importProcessInstance,
                BuildRecordUtil::buildProcessInstanceRecord,
                record -> record.getMetadata());

        processStream(zeebeKafkaStreams.kafkaZeebeTimerStream(),
                timerImporter::importTimer,
                BuildRecordUtil::buildTimerRecord,
                record -> record.getMetadata());

        processStream(zeebeKafkaStreams.kafkaZeebeVariableStream(),
                variableImporter::importVariable,
                BuildRecordUtil::buildVariableRecord,
                record -> record.getMetadata());

        processStream(zeebeKafkaStreams.kafkaZeebeErrorStream(),
                errorImporter::importError,
                BuildRecordUtil::buildErrorRecord,
                record -> record.getMetadata());

        processStream(zeebeKafkaStreams.kafkaZeebeMessageStream(),
                messageImporter::importMessage,
                BuildRecordUtil::buildMessageRecord,
                record -> record.getMetadata());

        processStream(zeebeKafkaStreams.kafkaZeebeIncidentStream(),
                incidentImporter::importIncident,
                BuildRecordUtil::buildIncidentRecord,
                record -> record.getMetadata());

        processStream(zeebeKafkaStreams.kafkaZeebeMessageSubscriptionStream(),
                messageSubscriptionImporter::importMessageSubscription,
                BuildRecordUtil::buildMessageSubscriptionRecord,
                record -> record.getMetadata());

        processStream(zeebeKafkaStreams.kafkaZeebeMessageSubscriptionStartEventStream(),
                messageSubscriptionImporter::importMessageStartEventSubscription,
                BuildRecordUtil::buildMessageStartEventSubscriptionRecord,
                record -> record.getMetadata());

        processStream(zeebeKafkaStreams.kafkaZeebeJobStream(),
                jobImporter::importJob,
                BuildRecordUtil::buildJobRecord,
                record -> record.getMetadata());
    }

    private <T> void processStream(
            KStream<String, GenericKafkaRecord> stream,
            Consumer<T> importer,
            Function<GenericKafkaRecord, T> recordBuilder,
            Function<T, Schema.RecordMetadata> metadataExtractor
    ) {
        stream
                .mapValues(recordBuilder::apply)
                .filter((key, record) -> ifEvent(record, metadataExtractor))
                .foreach((key, record) -> {
                    Future<?> future = executorService.submit(() -> importer.accept(record));
                    handleFuture(future);
                });
    }

    private void handleFuture(Future<?> future) {
        try {
            future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}