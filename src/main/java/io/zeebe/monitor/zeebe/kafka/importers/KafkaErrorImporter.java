package io.zeebe.monitor.zeebe.kafka.importers;

import io.zeebe.monitor.entity.ErrorEntity;
import io.zeebe.monitor.repository.ErrorRepository;
import io.zeebe.monitor.zeebe.kafka.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaErrorImporter {

  @Autowired private ErrorRepository errorRepository;

  public void importError(final GenericRecord record) {

    final var position = record.getPosition();
    Map values = record.getValue();
    final var entity =
        errorRepository
            .findById(position)
            .orElseGet(
                () -> {
                  final var newEntity = new ErrorEntity();
                  newEntity.setPosition(position);
                  newEntity.setErrorEventPosition(values.get("errorEventPosition") != null ? ((Number)values.get("errorEventPosition")).longValue() : 0);
                  newEntity.setProcessInstanceKey(values.get("processInstanceKey") != null ? ((Number)values.get("processInstanceKey")).longValue() : 0);
                  newEntity.setExceptionMessage((String)values.get("exceptionMessage"));
                  newEntity.setStacktrace((String)values.get("stacktrace"));
                  newEntity.setTimestamp(record.getTimestamp());
                  return newEntity;
                });

    errorRepository.save(entity);
  }

}
