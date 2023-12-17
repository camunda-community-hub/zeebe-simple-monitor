package io.zeebe.monitor.zeebe.kafka.importers;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.value.TimerRecordValue;
import io.zeebe.monitor.entity.TimerEntity;
import io.zeebe.monitor.repository.TimerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimerKafkaImporter extends KafkaImporter {

  @Autowired private TimerRepository timerRepository;

  @Override
  public void importRecord(final Record<RecordValue> record) {
    final var value = (TimerRecordValue) record.getValue();
    final var intent = record.getIntent();
    final long key = record.getKey();
    final long timestamp = record.getTimestamp();

    final var entity =
        timerRepository
            .findById(key)
            .orElseGet(
                () -> {
                  final var newEntity = new TimerEntity();
                  newEntity.setKey(key);
                  newEntity.setProcessDefinitionKey(value.getProcessDefinitionKey());
                  newEntity.setTargetElementId(value.getTargetElementId());
                  newEntity.setDueDate(value.getDueDate());
                  newEntity.setRepetitions(value.getRepetitions());

                  if (value.getProcessInstanceKey() > 0) {
                    newEntity.setProcessInstanceKey(value.getProcessInstanceKey());
                    newEntity.setElementInstanceKey(value.getElementInstanceKey());
                  }

                  return newEntity;
                });

    entity.setState(intent.name().toLowerCase());
    entity.setTimestamp(timestamp);
    timerRepository.save(entity);
  }
}
