package io.zeebe.monitor.zeebe.protobuf.importers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.ErrorEntity;
import io.zeebe.monitor.repository.ErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ErrorProtobufImporter {

  private final Counter counter;

  private final ErrorRepository errorRepository;

  @Autowired
  public ErrorHazelcastImporter(ErrorRepository errorRepository, MeterRegistry meterRegistry) {
    this.errorRepository = errorRepository;

    this.counter = Counter.builder("zeebemonitor_importer_error").description("number of processed errors").register(meterRegistry);
  }

  public void importError(final Schema.ErrorRecord record) {

    final var metadata = record.getMetadata();
    final var position = metadata.getPosition();

    final var entity =
        errorRepository
            .findById(position)
            .orElseGet(
                () -> {
                  final var newEntity = new ErrorEntity();
                  newEntity.setPosition(position);
                  newEntity.setErrorEventPosition(record.getErrorEventPosition());
                  newEntity.setProcessInstanceKey(record.getProcessInstanceKey());
                  newEntity.setExceptionMessage(record.getExceptionMessage());
                  newEntity.setStacktrace(record.getStacktrace());
                  newEntity.setTimestamp(metadata.getTimestamp());
                  return newEntity;
                });

    errorRepository.save(entity);

    counter.increment();
  }
}
