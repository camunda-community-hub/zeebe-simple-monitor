package io.zeebe.monitor.zeebe.kafka.importers;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.intent.IncidentIntent;
import io.camunda.zeebe.protocol.record.value.IncidentRecordValue;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.repository.IncidentRepository;
import io.zeebe.monitor.zeebe.event.IncidentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class IncidentKafkaImporter extends KafkaImporter {

  @Autowired private IncidentRepository incidentRepository;
  @Autowired private ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void importRecord(final Record<RecordValue> record) {
    final var value = (IncidentRecordValue) record.getValue();
    final var intent = record.getIntent();
    final long key = record.getKey();
    final long timestamp = record.getTimestamp();

    final var entity =
        incidentRepository
            .findById(key)
            .orElseGet(
                () -> {
                  final var newEntity = new IncidentEntity();
                  newEntity.setKey(key);
                  newEntity.setBpmnProcessId(value.getBpmnProcessId());
                  newEntity.setProcessDefinitionKey(value.getProcessDefinitionKey());
                  newEntity.setProcessInstanceKey(value.getProcessInstanceKey());
                  newEntity.setElementInstanceKey(value.getElementInstanceKey());
                  newEntity.setJobKey(value.getJobKey());
                  newEntity.setErrorType(
                      value.getErrorType() == null ? null : value.getErrorType().name());
                  newEntity.setErrorMessage(value.getErrorMessage());
                  return newEntity;
                });

    if (intent == IncidentIntent.CREATED) {
      entity.setCreated(timestamp);
      incidentRepository.save(entity);

      applicationEventPublisher.publishEvent(
          new IncidentEvent(
              entity.getBpmnProcessId(), value.getElementId(), IncidentIntent.CREATED));
    } else if (intent == IncidentIntent.RESOLVED) {
      entity.setResolved(timestamp);
      incidentRepository.save(entity);

      applicationEventPublisher.publishEvent(
          new IncidentEvent(
              entity.getBpmnProcessId(), value.getElementId(), IncidentIntent.RESOLVED));
    }
  }
}
