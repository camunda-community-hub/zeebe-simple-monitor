package io.zeebe.monitor.zeebe.kafka.importers;

import io.camunda.zeebe.protocol.record.intent.MessageIntent;
import io.zeebe.monitor.entity.MessageEntity;
import io.zeebe.monitor.repository.MessageRepository;
import io.zeebe.monitor.zeebe.kafka.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaMessageImporter {

  @Autowired private MessageRepository messageRepository;

  public void importMessage(final GenericRecord record) {

    final MessageIntent intent = MessageIntent.valueOf(record.getIntent());
    final long key = record.getKey();
    final long timestamp = record.getTimestamp();
    Map values = record.getValue();
    final MessageEntity entity =
        messageRepository
            .findById(key)
            .orElseGet(
                () -> {
                  final MessageEntity newEntity = new MessageEntity();
                  newEntity.setKey(key);
                  newEntity.setName((String)values.get("name"));
                  newEntity.setCorrelationKey((String)values.get("correlationKey"));
                  newEntity.setMessageId((String)values.get("messageId"));
                  newEntity.setPayload((String)values.get("variables"));
                  return newEntity;
                });

    entity.setState(intent.name().toLowerCase());
    entity.setTimestamp(timestamp);
    messageRepository.save(entity);
  }
}
