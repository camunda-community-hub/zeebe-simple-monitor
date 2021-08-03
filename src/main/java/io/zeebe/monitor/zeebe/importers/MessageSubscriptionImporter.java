package io.zeebe.monitor.zeebe.importers;

import io.camunda.zeebe.protocol.record.intent.MessageStartEventSubscriptionIntent;
import io.camunda.zeebe.protocol.record.intent.MessageSubscriptionIntent;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.MessageSubscriptionEntity;
import io.zeebe.monitor.repository.MessageSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MessageSubscriptionImporter {

  @Autowired private MessageSubscriptionRepository messageSubscriptionRepository;

  public void importMessageSubscription(final Schema.MessageSubscriptionRecord record) {

    final MessageSubscriptionIntent intent =
        MessageSubscriptionIntent.valueOf(record.getMetadata().getIntent());
    final long timestamp = record.getMetadata().getTimestamp();

    final MessageSubscriptionEntity entity =
        messageSubscriptionRepository
            .findByElementInstanceKeyAndMessageName(
                record.getElementInstanceKey(), record.getMessageName())
            .orElseGet(
                () -> {
                  final MessageSubscriptionEntity newEntity = new MessageSubscriptionEntity();
                  newEntity.setId(
                      generateId()); // message subscription doesn't have a key - it is always '-1'
                  newEntity.setElementInstanceKey(record.getElementInstanceKey());
                  newEntity.setMessageName(record.getMessageName());
                  newEntity.setCorrelationKey(record.getCorrelationKey());
                  newEntity.setProcessInstanceKey(record.getProcessInstanceKey());
                  return newEntity;
                });

    entity.setState(intent.name().toLowerCase());
    entity.setTimestamp(timestamp);
    messageSubscriptionRepository.save(entity);
  }

  public void importMessageStartEventSubscription(
      final Schema.MessageStartEventSubscriptionRecord record) {

    final MessageStartEventSubscriptionIntent intent =
        MessageStartEventSubscriptionIntent.valueOf(record.getMetadata().getIntent());
    final long timestamp = record.getMetadata().getTimestamp();

    final MessageSubscriptionEntity entity =
        messageSubscriptionRepository
            .findByProcessDefinitionKeyAndMessageName(
                record.getProcessDefinitionKey(), record.getMessageName())
            .orElseGet(
                () -> {
                  final MessageSubscriptionEntity newEntity = new MessageSubscriptionEntity();
                  newEntity.setId(
                      generateId()); // message subscription doesn't have a key - it is always '-1'
                  newEntity.setMessageName(record.getMessageName());
                  newEntity.setProcessDefinitionKey(record.getProcessDefinitionKey());
                  newEntity.setTargetFlowNodeId(record.getStartEventId());
                  return newEntity;
                });

    entity.setState(intent.name().toLowerCase());
    entity.setTimestamp(timestamp);
    messageSubscriptionRepository.save(entity);
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }

}
