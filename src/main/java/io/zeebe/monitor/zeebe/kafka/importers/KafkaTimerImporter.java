package io.zeebe.monitor.zeebe.kafka.importers;

import io.camunda.zeebe.protocol.record.intent.TimerIntent;
import io.zeebe.monitor.entity.TimerEntity;
import io.zeebe.monitor.repository.TimerRepository;
import io.zeebe.monitor.zeebe.kafka.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaTimerImporter {

  @Autowired private TimerRepository timerRepository;

  public void importTimer(final GenericRecord record) {

    final TimerIntent intent = TimerIntent.valueOf(record.getIntent());
    final long key = record.getKey();
    final long timestamp = record.getTimestamp();
    Map values = record.getValue();
    final TimerEntity entity =
        timerRepository
            .findById(key)
            .orElseGet(
                () -> {
                  final TimerEntity newEntity = new TimerEntity();
                  newEntity.setKey(key);
                  newEntity.setProcessDefinitionKey(values.get("processDefinitionKey") != null ? ((Number)values.get("processDefinitionKey")).longValue() : 0);
                  newEntity.setTargetElementId((String)values.get("targetElementId"));
                  newEntity.setDueDate(values.get("dueDate") != null ? ((Number)values.get("dueDate")).longValue() : 0);
                  newEntity.setRepetitions(values.get("repetitions") != null ? ((Number)values.get("repetitions")).intValue(): 0);

                  Long processInstanceKey = values.get("processInstanceKey") != null ? ((Number)values.get("processInstanceKey")).longValue() : 0;
                  if (processInstanceKey > 0) {
                    newEntity.setProcessInstanceKey(processInstanceKey);
                    newEntity.setElementInstanceKey(values.get("elementInstanceKey") != null ? ((Number)values.get("elementInstanceKey")).longValue() : 0);
                  }

                  return newEntity;
                });

    entity.setState(intent.name().toLowerCase());
    entity.setTimestamp(timestamp);
    timerRepository.save(entity);
  }

}
