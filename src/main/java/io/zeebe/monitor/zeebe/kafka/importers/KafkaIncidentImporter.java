package io.zeebe.monitor.zeebe.kafka.importers;

import io.camunda.zeebe.protocol.record.intent.IncidentIntent;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.repository.IncidentRepository;
import io.zeebe.monitor.zeebe.kafka.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaIncidentImporter {

  @Autowired private IncidentRepository incidentRepository;

  public void importIncident(final GenericRecord record) {

    final IncidentIntent intent = IncidentIntent.valueOf(record.getIntent());
    final long key = record.getKey();
    final long timestamp = record.getTimestamp();

    Map values = record.getValue();

    final IncidentEntity entity =
        incidentRepository
            .findById(key)
            .orElseGet(
                () -> {
                  final IncidentEntity newEntity = new IncidentEntity();
                  newEntity.setKey(key);
                  newEntity.setBpmnProcessId((String)values.get("bpmnProcessId"));
                  newEntity.setProcessDefinitionKey(values.get("processDefinitionKey") != null ? ((Number)values.get("processDefinitionKey")).longValue() : 0);
                  newEntity.setProcessInstanceKey(values.get("processInstanceKey") != null ? ((Number)values.get("processInstanceKey")).longValue() : 0);
                  newEntity.setElementInstanceKey(values.get("elementInstanceKey") != null ? ((Number)values.get("elementInstanceKey")).longValue() : 0);
                  newEntity.setJobKey(values.get("jobKey") != null ? ((Number)values.get("jobKey")).longValue() : 0);
                  newEntity.setErrorType((String)values.get("errorType"));
                  newEntity.setErrorMessage((String)values.get("errorMessage"));
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
