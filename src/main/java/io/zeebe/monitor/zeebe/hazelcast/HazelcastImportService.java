package io.zeebe.monitor.zeebe.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.hazelcast.connect.java.ZeebeHazelcast;
import io.zeebe.monitor.zeebe.protobuf.importers.ErrorProtobufImporter;
import io.zeebe.monitor.zeebe.protobuf.importers.IncidentProtobufImporter;
import io.zeebe.monitor.zeebe.protobuf.importers.JobProtobufImporter;
import io.zeebe.monitor.zeebe.protobuf.importers.MessageProtobufImporter;
import io.zeebe.monitor.zeebe.protobuf.importers.MessageSubscriptionProtobufImporter;
import io.zeebe.monitor.zeebe.protobuf.importers.ProcessAndElementProtobufImporter;
import io.zeebe.monitor.zeebe.protobuf.importers.TimerProtobufImporter;
import io.zeebe.monitor.zeebe.protobuf.importers.VariableProtobufImporter;
import java.util.function.Consumer;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HazelcastImportService {

  @Autowired private ProcessAndElementProtobufImporter processAndElementImporter;
  @Autowired private VariableProtobufImporter variableImporter;
  @Autowired private JobProtobufImporter jobImporter;
  @Autowired private IncidentProtobufImporter incidentImporter;
  @Autowired private MessageProtobufImporter messageImporter;
  @Autowired private MessageSubscriptionProtobufImporter messageSubscriptionImporter;
  @Autowired private TimerProtobufImporter timerImporter;
  @Autowired private ErrorProtobufImporter errorImporter;

  @Autowired private HazelcastStateService hazelcastStateService;

  public ZeebeHazelcast importFrom(final HazelcastInstance hazelcast) {
    final var builder =
        ZeebeHazelcast.newBuilder(hazelcast)
            .addProcessListener(
                record ->
                    ifEvent(
                        record,
                        Schema.ProcessRecord::getMetadata,
                        processAndElementImporter::importProcess))
            .addProcessInstanceListener(
                record ->
                    ifEvent(
                        record,
                        Schema.ProcessInstanceRecord::getMetadata,
                        processAndElementImporter::importProcessInstance))
            .addIncidentListener(
                record ->
                    ifEvent(
                        record,
                        Schema.IncidentRecord::getMetadata,
                        incidentImporter::importIncident))
            .addJobListener(
                record -> ifEvent(record, Schema.JobRecord::getMetadata, jobImporter::importJob))
            .addVariableListener(
                record ->
                    ifEvent(
                        record,
                        Schema.VariableRecord::getMetadata,
                        variableImporter::importVariable))
            .addTimerListener(
                record ->
                    ifEvent(record, Schema.TimerRecord::getMetadata, timerImporter::importTimer))
            .addMessageListener(
                record ->
                    ifEvent(
                        record, Schema.MessageRecord::getMetadata, messageImporter::importMessage))
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
            .postProcessListener(hazelcastStateService::saveSequenceNumber);

    final var lastSequence = hazelcastStateService.getLastSequenceNumber();
    if (lastSequence >= 0) {
      builder.readFrom(lastSequence);
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
