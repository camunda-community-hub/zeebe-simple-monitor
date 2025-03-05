package io.zeebe.monitor.zeebe.protobuf.importers;

import io.camunda.zeebe.protocol.record.intent.MessageStartEventSubscriptionIntent;
import io.camunda.zeebe.protocol.record.intent.MessageSubscriptionIntent;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.MessageSubscriptionEntity;
import io.zeebe.monitor.repository.MessageSubscriptionRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageSubscriptionProtobufImporter {

  @Autowired
  private MessageSubscriptionRepository messageSubscriptionRepository;

  public void importMessageSubscription(final Schema.MessageSubscriptionRecord record) {

    final MessageSubscriptionIntent intent = MessageSubscriptionIntent.valueOf(record.getMetadata().getIntent());
    if (intent != MessageSubscriptionIntent.CREATED && intent != MessageSubscriptionIntent.CORRELATED) {
      return;

    }
    final long timestamp = record.getMetadata().getTimestamp();

    final MessageSubscriptionEntity newEntity = new MessageSubscriptionEntity();
    newEntity.setId(
        generateId()); // message subscription doesn't have a key - it is always '-1'
    newEntity.setElementInstanceKey(record.getElementInstanceKey());
    newEntity.setMessageName(record.getMessageName());
    newEntity.setCorrelationKey(record.getCorrelationKey());
    newEntity.setProcessInstanceKey(record.getProcessInstanceKey());

    newEntity.setState(intent.name().toLowerCase());
    newEntity.setTimestamp(timestamp);
    messageSubscriptionRepository.save(newEntity);
  }

  public void importMessageStartEventSubscription(
      final Schema.MessageStartEventSubscriptionRecord record) {

    final MessageStartEventSubscriptionIntent intent = MessageStartEventSubscriptionIntent
        .valueOf(record.getMetadata().getIntent());
    if (intent != MessageStartEventSubscriptionIntent.CORRELATED) {
      return;

    }
    final long timestamp = record.getMetadata().getTimestamp();

    final MessageSubscriptionEntity newEntity = new MessageSubscriptionEntity();
    newEntity.setId(
        generateId()); // message subscription doesn't have a key - it is always '-1'
    newEntity.setMessageName(record.getMessageName());
    newEntity.setProcessDefinitionKey(record.getProcessDefinitionKey());
    newEntity.setTargetFlowNodeId(record.getStartEventId());

    newEntity.setState(intent.name().toLowerCase());
    newEntity.setTimestamp(timestamp);
    messageSubscriptionRepository.save(newEntity);
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
