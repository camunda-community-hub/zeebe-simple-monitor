package io.zeebe.monitor.zeebe.protobuf.importers;

import io.camunda.zeebe.protocol.record.intent.MessageIntent;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.MessageEntity;
import io.zeebe.monitor.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageProtobufImporter {

  @Autowired
  private MessageRepository messageRepository;

  public void importMessage(final Schema.MessageRecord record) {

    final MessageIntent intent = MessageIntent.valueOf(record.getMetadata().getIntent());
    if (intent != MessageIntent.PUBLISHED) {
      return;

    }
    final long key = record.getMetadata().getKey();
    final long timestamp = record.getMetadata().getTimestamp();

    final MessageEntity newEntity = new MessageEntity();
    newEntity.setKey(key);
    newEntity.setName(record.getName());
    newEntity.setCorrelationKey(record.getCorrelationKey());
    newEntity.setMessageId(record.getMessageId());
    newEntity.setPayload(record.getVariables().toString());

    newEntity.setState(intent.name().toLowerCase());
    newEntity.setTimestamp(timestamp);
    messageRepository.save(newEntity);
  }
}
