package io.zeebe.monitor.zeebe.kafka.importers;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.value.MessageStartEventSubscriptionRecordValue;
import io.zeebe.monitor.entity.MessageSubscriptionEntity;
import io.zeebe.monitor.repository.MessageSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageStartEventSubscriptionKafkaImporter extends KafkaImporter {

  @Autowired private MessageSubscriptionRepository messageSubscriptionRepository;

  @Override
  public void importRecord(final Record<RecordValue> record) {
    final var value = (MessageStartEventSubscriptionRecordValue) record.getValue();
    final var intent = record.getIntent();
    final long timestamp = record.getTimestamp();

    final var entity =
        messageSubscriptionRepository
            .findByProcessDefinitionKeyAndMessageName(
                value.getProcessDefinitionKey(), value.getMessageName())
            .orElseGet(
                () -> {
                  final var newEntity = new MessageSubscriptionEntity();
                  // message subscription doesn't have a key - it is always '-1'
                  newEntity.setId(generateId());
                  newEntity.setMessageName(value.getMessageName());
                  newEntity.setProcessDefinitionKey(value.getProcessDefinitionKey());
                  newEntity.setTargetFlowNodeId(value.getStartEventId());
                  return newEntity;
                });

    entity.setState(intent.name().toLowerCase());
    entity.setTimestamp(timestamp);
    messageSubscriptionRepository.save(entity);
  }
}
