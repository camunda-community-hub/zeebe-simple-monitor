package io.zeebe.monitor.zeebe.protobuf.importers;

import io.camunda.zeebe.protocol.record.intent.IncidentIntent;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IncidentProtobufImporter {

  @Autowired private IncidentRepository incidentRepository;

  public void importIncident(final Schema.IncidentRecord record) {

    final IncidentIntent intent = IncidentIntent.valueOf(record.getMetadata().getIntent());
    if (intent!= IncidentIntent.CREATED && intent != IncidentIntent.RESOLVED) {
      return;
    }
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

    } else if (intent == IncidentIntent.RESOLVED) {
      entity.setResolved(timestamp);
      incidentRepository.save(entity);
    }
  }
}
