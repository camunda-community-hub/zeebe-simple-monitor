package io.zeebe.monitor.zeebe.kafka.importers;

import io.camunda.zeebe.protocol.record.intent.MessageStartEventSubscriptionIntent;
import io.camunda.zeebe.protocol.record.intent.MessageSubscriptionIntent;
import io.zeebe.monitor.entity.MessageSubscriptionEntity;
import io.zeebe.monitor.repository.MessageSubscriptionRepository;
import io.zeebe.monitor.zeebe.kafka.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class KafkaMessageSubscriptionImporter {

  @Autowired private MessageSubscriptionRepository messageSubscriptionRepository;

  public void importMessageSubscription(final GenericRecord record) {

    final MessageSubscriptionIntent intent =
        MessageSubscriptionIntent.valueOf(record.getIntent());
    final long timestamp = record.getTimestamp();
    Map values = record.getValue();
    final MessageSubscriptionEntity entity =
        messageSubscriptionRepository
            .findByElementInstanceKeyAndMessageName(
                    values.get("elementInstanceKey") != null ? (Long) values.get("elementInstanceKey") : 0, (String)values.get("messageName"))
            .orElseGet(
                () -> {
                  final MessageSubscriptionEntity newEntity = new MessageSubscriptionEntity();
                  newEntity.setId(
                      generateId()); // message subscription doesn't have a key - it is always '-1'
                  newEntity.setElementInstanceKey(values.get("elementInstanceKey") != null ? (Long)values.get("elementInstanceKey") : 0);
                  newEntity.setMessageName((String)values.get("messageName"));
                  newEntity.setCorrelationKey((String)values.get("correlationKey"));
                  newEntity.setProcessInstanceKey(values.get("processInstanceKey") != null ? (Long)values.get("processInstanceKey") : 0);
                  return newEntity;
                });

    entity.setState(intent.name().toLowerCase());
    entity.setTimestamp(timestamp);
    messageSubscriptionRepository.save(entity);
  }

  public void importMessageStartEventSubscription(
      final GenericRecord record) {

    final MessageStartEventSubscriptionIntent intent =
        MessageStartEventSubscriptionIntent.valueOf(record.getIntent());
    final long timestamp = record.getTimestamp();
    Map values = record.getValue();
    final MessageSubscriptionEntity entity =
        messageSubscriptionRepository
            .findByProcessDefinitionKeyAndMessageName(
                values.get("processDefinitionKey") != null ? ((Number) values.get("processDefinitionKey")).longValue() : 0, (String)values.get("messageName"))
            .orElseGet(
                () -> {
                  final MessageSubscriptionEntity newEntity = new MessageSubscriptionEntity();
                  newEntity.setId(
                      generateId()); // message subscription doesn't have a key - it is always '-1'
                  newEntity.setMessageName((String)values.get("messageName"));
                  newEntity.setProcessDefinitionKey(values.get("processDefinitionKey") != null ? ((Number)values.get("processDefinitionKey")).longValue() : 0);
                  newEntity.setTargetFlowNodeId((String)values.get("startEventId"));
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
