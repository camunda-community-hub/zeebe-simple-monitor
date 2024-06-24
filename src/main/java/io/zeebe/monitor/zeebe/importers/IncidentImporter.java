package io.zeebe.monitor.zeebe.importers;

import io.camunda.zeebe.protocol.record.intent.IncidentIntent;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.repository.IncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class IncidentImporter {

    @Autowired
    private IncidentRepository incidentRepository;

    private static final Logger LOG = LoggerFactory.getLogger(IncidentImporter.class);

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
            try {
                incidentRepository.save(entity);
            } catch (DataIntegrityViolationException e) {
                LOG.warn("Attempted to save duplicate Incident with key {}", entity.getKey());
            }

        } else if (intent == IncidentIntent.RESOLVED) {
            entity.setResolved(timestamp);
            try {
                incidentRepository.save(entity);
            } catch (DataIntegrityViolationException e) {
                LOG.warn("Attempted to save duplicate Incident with key {}", entity.getKey());
            }
        }
    }

}
