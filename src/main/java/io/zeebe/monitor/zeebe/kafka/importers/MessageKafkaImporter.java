package io.zeebe.monitor.zeebe.kafka.importers;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.value.MessageRecordValue;
import io.zeebe.monitor.entity.MessageEntity;
import io.zeebe.monitor.repository.MessageRepository;
import io.zeebe.monitor.zeebe.event.MessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class MessageKafkaImporter extends KafkaImporter {

  @Autowired private MessageRepository messageRepository;
  @Autowired private ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void importRecord(final Record<RecordValue> record) {
    final var value = (MessageRecordValue) record.getValue();
    final var intent = record.getIntent();
    final long key = record.getKey();
    final long timestamp = record.getTimestamp();

    final var entity =
        messageRepository
            .findById(key)
            .orElseGet(
                () -> {
                  final var newEntity = new MessageEntity();
                  newEntity.setKey(key);
                  newEntity.setName(value.getName());
                  newEntity.setCorrelationKey(value.getCorrelationKey());
                  newEntity.setMessageId(value.getMessageId());
                  newEntity.setPayload(value.getVariables().toString());
                  return newEntity;
                });

    entity.setState(intent.name().toLowerCase());
    entity.setTimestamp(timestamp);
    messageRepository.save(entity);

    applicationEventPublisher.publishEvent(new MessageEvent());
  }
}
