package io.zeebe.monitor.zeebe.importers;

import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.TimerEntity;
import io.zeebe.monitor.repository.TimerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimerImporter {

  @Autowired private TimerRepository timerRepository;

  public void importTimer(final Schema.TimerRecord record) {

    final String intent = record.getMetadata().getIntent();
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

    entity.setState(intent.toLowerCase());
    entity.setTimestamp(timestamp);
    timerRepository.save(entity);
  }

}
