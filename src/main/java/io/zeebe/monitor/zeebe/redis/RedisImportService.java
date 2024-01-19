package io.zeebe.monitor.zeebe.redis;

import com.hazelcast.core.HazelcastInstance;
import io.lettuce.core.RedisClient;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.hazelcast.connect.java.ZeebeHazelcast;
import io.zeebe.monitor.config.RedisConfig;
import io.zeebe.monitor.entity.HazelcastConfig;
import io.zeebe.monitor.repository.HazelcastConfigRepository;
import io.zeebe.monitor.zeebe.protobuf.importers.*;
import io.zeebe.redis.connect.java.ZeebeRedis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class RedisImportService {

  @Autowired private ProcessAndElementProtobufImporter processAndElementImporter;
  @Autowired private VariableProtobufImporter variableImporter;
  @Autowired private JobProtobufImporter jobImporter;
  @Autowired private IncidentProtobufImporter incidentImporter;
  @Autowired private MessageProtobufImporter messageImporter;
  @Autowired private MessageSubscriptionProtobufImporter messageSubscriptionImporter;
  @Autowired private TimerProtobufImporter timerImporter;
  @Autowired private ErrorProtobufImporter errorImporter;

  public ZeebeRedis importFrom(final RedisClient redisClient, RedisConfig redisConfig) {

   final var builder =
        ZeebeRedis.newBuilder(redisClient)
            .consumerGroup(redisConfig.getRedisConumerGroup())
            .xreadCount(redisConfig.getRedisXreadCount()).xreadBlockMillis(redisConfig.getRedisXreadBlockMillis())
            .addProcessListener(
                record -> ifEvent(record, Schema.ProcessRecord::getMetadata, processAndElementImporter::importProcess))
            .addProcessInstanceListener(
                record ->
                    ifEvent(
                        record,
                        Schema.ProcessInstanceRecord::getMetadata,
                        processAndElementImporter::importProcessInstance))
            .addIncidentListener(
                record -> ifEvent(record, Schema.IncidentRecord::getMetadata, incidentImporter::importIncident))
            .addJobListener(
                record -> ifEvent(record, Schema.JobRecord::getMetadata, jobImporter::importJob))
            .addVariableListener(
                record -> ifEvent(record, Schema.VariableRecord::getMetadata, variableImporter::importVariable))
            .addTimerListener(
                record -> ifEvent(record, Schema.TimerRecord::getMetadata, timerImporter::importTimer))
            .addMessageListener(
                record -> ifEvent(record, Schema.MessageRecord::getMetadata, messageImporter::importMessage))
            .addMessageSubscriptionListener(
                record ->
                    ifEvent(
                        record,
                        Schema.MessageSubscriptionRecord::getMetadata,
                            messageSubscriptionImporter::importMessageSubscription))
            .addMessageStartEventSubscriptionListener(
                record ->
                    ifEvent(
                        record,
                        Schema.MessageStartEventSubscriptionRecord::getMetadata,
                            messageSubscriptionImporter::importMessageStartEventSubscription))
            .addErrorListener(errorImporter::importError);

    return builder.build();
  }

  private <T> void ifEvent(
      final T record,
      final Function<T, Schema.RecordMetadata> extractor,
      final Consumer<T> consumer) {
    final var metadata = extractor.apply(record);
    if (isEvent(metadata)) {
      consumer.accept(record);
    }
  }

  private boolean isEvent(final Schema.RecordMetadata metadata) {
    return metadata.getRecordType() == Schema.RecordMetadata.RecordType.EVENT;
  }

}
