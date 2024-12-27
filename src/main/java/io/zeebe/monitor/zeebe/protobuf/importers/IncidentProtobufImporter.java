package io.zeebe.monitor.zeebe.protobuf.importers;

import io.camunda.zeebe.protocol.record.intent.IncidentIntent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IncidentProtobufImporter {

  private final IncidentRepository incidentRepository;
  private final Counter createdCounter;
  private final Counter resolvedCounter;

  @Autowired
  public IncidentProtobufImporter(IncidentRepository incidentRepository, MeterRegistry meterRegistry) {
    this.incidentRepository = incidentRepository;

    createdCounter = Counter.builder("zeebemonitor_importer_incident").tag("action", "created").description("number of processed incidents").register(meterRegistry);
    resolvedCounter = Counter.builder("zeebemonitor_importer_incident").tag("action", "resolved").description("number of processed incidents").register(meterRegistry);
  }

  public void importIncident(final Schema.IncidentRecord record) {

    final IncidentIntent intent = IncidentIntent.valueOf(record.getMetadata().getIntent());
    final long key = record.getMetadata().getKey();
    final long timestamp = record.getMetadata().getTimestamp();

    final IncidentEntity entity =
        incidentRepository
            .findById(key)
            .orElseGet(
                () -> {
                  final IncidentEntity newEntity = new IncidentEntity();
                  newEntity.setKey(key);
                  newEntity.setBpmnProcessId(record.getBpmnProcessId());
                  newEntity.setProcessDefinitionKey(record.getProcessDefinitionKey());
                  newEntity.setProcessInstanceKey(record.getProcessInstanceKey());
                  newEntity.setElementInstanceKey(record.getElementInstanceKey());
                  newEntity.setJobKey(record.getJobKey());
                  newEntity.setErrorType(record.getErrorType());
                  newEntity.setErrorMessage(record.getErrorMessage());
                  return newEntity;
                });

    if (intent == IncidentIntent.CREATED) {
      entity.setCreated(timestamp);
      incidentRepository.save(entity);

      createdCounter.increment();

    } else if (intent == IncidentIntent.RESOLVED) {
      entity.setResolved(timestamp);
      incidentRepository.save(entity);

      resolvedCounter.increment();
    }
  }
}
