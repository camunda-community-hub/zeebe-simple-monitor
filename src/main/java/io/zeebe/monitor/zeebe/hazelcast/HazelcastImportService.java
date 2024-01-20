package io.zeebe.monitor.zeebe.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.hazelcast.connect.java.ZeebeHazelcast;
import io.zeebe.monitor.entity.HazelcastConfig;
import io.zeebe.monitor.repository.HazelcastConfigRepository;
import io.zeebe.monitor.zeebe.hazelcast.importers.ErrorHazelcastImporter;
import io.zeebe.monitor.zeebe.hazelcast.importers.IncidentHazelcastImporter;
import io.zeebe.monitor.zeebe.hazelcast.importers.JobHazelcastImporter;
import io.zeebe.monitor.zeebe.hazelcast.importers.MessageHazelcastImporter;
import io.zeebe.monitor.zeebe.hazelcast.importers.MessageSubscriptionHazelcastImporter;
import io.zeebe.monitor.zeebe.hazelcast.importers.ProcessAndElementHazelcastImporter;
import io.zeebe.monitor.zeebe.hazelcast.importers.TimerHazelcastImporter;
import io.zeebe.monitor.zeebe.hazelcast.importers.VariableHazelcastImporter;
import java.util.function.Consumer;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HazelcastImportService {

  @Autowired private ProcessAndElementHazelcastImporter processAndElementImporter;
  @Autowired private VariableHazelcastImporter variableImporter;
  @Autowired private JobHazelcastImporter jobImporter;
  @Autowired private IncidentHazelcastImporter incidentImporter;
  @Autowired private MessageHazelcastImporter messageImporter;
  @Autowired private MessageSubscriptionHazelcastImporter messageSubscriptionImporter;
  @Autowired private TimerHazelcastImporter timerImporter;
  @Autowired private ErrorHazelcastImporter errorImporter;

  @Autowired private HazelcastConfigRepository hazelcastConfigRepository;

  public ZeebeHazelcast importFrom(final HazelcastInstance hazelcast) {

    final var hazelcastConfig =
        hazelcastConfigRepository
            .findById("cfg")
            .orElseGet(
                () -> {
                  final var config = new HazelcastConfig();
                  config.setId("cfg");
                  config.setSequence(-1);
                  return config;
                });

    final var builder =
        ZeebeHazelcast.newBuilder(hazelcast)
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
            .addErrorListener(errorImporter::importError)
            .postProcessListener(
                sequence -> {
                  hazelcastConfig.setSequence(sequence);
                  hazelcastConfigRepository.save(hazelcastConfig);
                });

    if (hazelcastConfig.getSequence() >= 0) {
      builder.readFrom(hazelcastConfig.getSequence());
    } else {
      builder.readFromHead();
    }

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
