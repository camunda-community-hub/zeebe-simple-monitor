package io.zeebe.monitor.zeebe.importers;

import io.camunda.zeebe.protocol.record.intent.TimerIntent;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.TimerEntity;
import io.zeebe.monitor.repository.TimerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class TimerImporter {

    private static final Logger LOG = LoggerFactory.getLogger(TimerImporter.class);
    @Autowired
    private TimerRepository timerRepository;

    public void importTimer(final Schema.TimerRecord record) {

        final TimerIntent intent = TimerIntent.valueOf(record.getMetadata().getIntent());
        final long key = record.getMetadata().getKey();
        final long timestamp = record.getMetadata().getTimestamp();

        final TimerEntity entity =
                timerRepository
                        .findById(key)
                        .orElseGet(
                                () -> {
                                    final TimerEntity newEntity = new TimerEntity();
                                    newEntity.setKey(key);
                                    newEntity.setProcessDefinitionKey(record.getProcessDefinitionKey());
                                    newEntity.setTargetElementId(record.getTargetElementId());
                                    newEntity.setDueDate(record.getDueDate());
                                    newEntity.setRepetitions(record.getRepetitions());

                                    if (record.getProcessInstanceKey() > 0) {
                                        newEntity.setProcessInstanceKey(record.getProcessInstanceKey());
                                        newEntity.setElementInstanceKey(record.getElementInstanceKey());
                                    }

                                    return newEntity;
                                });

        entity.setState(intent.name().toLowerCase());
        entity.setTimestamp(timestamp);
        try {
            timerRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            LOG.warn("Attempted to save duplicate Timer with key {}", entity.getKey());
        }
    }

}
