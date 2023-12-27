package io.zeebe.monitor.zeebe.kafka.importers;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.value.ErrorRecordValue;
import io.zeebe.monitor.entity.ErrorEntity;
import io.zeebe.monitor.repository.ErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ErrorKafkaImporter extends KafkaImporter {

  @Autowired private ErrorRepository errorRepository;

  @Override
  public void importRecord(final Record<RecordValue> record) {
    final var value = (ErrorRecordValue) record.getValue();
    final var position = record.getPosition();

    final var entity =
        errorRepository
            .findById(position)
            .orElseGet(
                () -> {
                  final var newEntity = new ErrorEntity();
                  newEntity.setPosition(position);
                  newEntity.setErrorEventPosition(value.getErrorEventPosition());
                  newEntity.setProcessInstanceKey(value.getProcessInstanceKey());
                  newEntity.setExceptionMessage(value.getExceptionMessage());
                  newEntity.setStacktrace(value.getStacktrace());
                  newEntity.setTimestamp(record.getTimestamp());
                  return newEntity;
                });

    errorRepository.save(entity);
  }
}
