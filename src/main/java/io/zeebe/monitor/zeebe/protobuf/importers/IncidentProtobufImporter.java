package io.zeebe.monitor.zeebe.protobuf.importers;

import io.camunda.zeebe.protocol.record.intent.IncidentIntent;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.repository.IncidentRepository;
import io.zeebe.monitor.zeebe.event.IncidentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class IncidentProtobufImporter {

  private final IncidentRepository incidentRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public IncidentProtobufImporter(
      IncidentRepository incidentRepository, ApplicationEventPublisher applicationEventPublisher) {
    this.incidentRepository = incidentRepository;
    this.applicationEventPublisher = applicationEventPublisher;
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

      applicationEventPublisher.publishEvent(
          new IncidentEvent(
              entity.getBpmnProcessId(), record.getElementId(), IncidentIntent.CREATED));
    } else if (intent == IncidentIntent.RESOLVED) {
      entity.setResolved(timestamp);
      incidentRepository.save(entity);

      applicationEventPublisher.publishEvent(
          new IncidentEvent(
              entity.getBpmnProcessId(), record.getElementId(), IncidentIntent.RESOLVED));
    }
  }
}
