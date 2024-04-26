package io.zeebe.monitor.zeebe.protobuf.importers;

import io.camunda.zeebe.protocol.record.intent.MessageStartEventSubscriptionIntent;
import io.camunda.zeebe.protocol.record.intent.MessageSubscriptionIntent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.MessageSubscriptionEntity;
import io.zeebe.monitor.repository.MessageSubscriptionRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageSubscriptionProtobufImporter {

  private final MessageSubscriptionRepository messageSubscriptionRepository;
  private final Counter subsCounter;
  private final Counter eventCounter;

  @Autowired
  public MessageSubscriptionHazelcastImporter(MessageSubscriptionRepository messageSubscriptionRepository, MeterRegistry meterRegistry) {
    this.messageSubscriptionRepository = messageSubscriptionRepository;

    this.subsCounter =
            Counter.builder("zeebemonitor_importer_message_subscription").description("number of processed message subscriptions").register(meterRegistry);
    this.eventCounter = Counter.builder("zeebemonitor_importer_message_start_event_subscription").description("number of processed message start events").register(meterRegistry);
  }

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


    subsCounter.increment();
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

    eventCounter.increment();
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
