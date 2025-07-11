package io.zeebe.monitor.zeebe.protobuf.importers;

import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.ErrorEntity;
import io.zeebe.monitor.repository.ErrorRepository;
import io.zeebe.monitor.zeebe.event.ErrorEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ErrorProtobufImporter {

  private final ErrorRepository errorRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public ErrorProtobufImporter(
      ErrorRepository errorRepository, ApplicationEventPublisher applicationEventPublisher) {
    this.errorRepository = errorRepository;
    this.applicationEventPublisher = applicationEventPublisher;
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

    applicationEventPublisher.publishEvent(new ErrorEvent());
  }
}
