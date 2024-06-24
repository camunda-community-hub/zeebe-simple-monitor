package io.zeebe.monitor.zeebe.importers;

import io.camunda.zeebe.protocol.record.intent.MessageStartEventSubscriptionIntent;
import io.camunda.zeebe.protocol.record.intent.MessageSubscriptionIntent;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.MessageSubscriptionEntity;
import io.zeebe.monitor.repository.MessageSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class MessageSubscriptionImporter {

    private static final Logger LOG = LoggerFactory.getLogger(MessageImporter.class);

    @Autowired
    private MessageSubscriptionRepository messageSubscriptionRepository;

    public void importMessageSubscription(final Schema.MessageSubscriptionRecord record) {

        final MessageSubscriptionIntent intent = MessageSubscriptionIntent.valueOf(record.getMetadata().getIntent());
        final long timestamp = record.getMetadata().getTimestamp();

        final MessageSubscriptionEntity entity =
                messageSubscriptionRepository
                        .findByElementInstanceKeyAndMessageName(record.getElementInstanceKey(), record.getMessageName())
                        .orElseGet(
                                () -> {
                                    final MessageSubscriptionEntity newEntity = new MessageSubscriptionEntity();
                                    // need to build a logical id since message subscription doesn't have a key - it is always '-1'
                                    newEntity.setId(
                                            record.getElementInstanceKey() + "-" + record.getMessageName() + "-" + record.getProcessInstanceKey());
                                    newEntity.setElementInstanceKey(record.getElementInstanceKey());
                                    newEntity.setMessageName(record.getMessageName());
                                    newEntity.setCorrelationKey(record.getCorrelationKey());
                                    newEntity.setProcessInstanceKey(record.getProcessInstanceKey());
                                    return newEntity;
                                });

        entity.setState(intent.name().toLowerCase());
        entity.setTimestamp(timestamp);
        try {
            messageSubscriptionRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            LOG.warn("Attempted to save duplicate MessageSubscription with id {}", entity.getId());
        }
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
                                    // need to build a logical id since message subscription doesn't have a key - it is always '-1'
                                    newEntity.setId(
                                            record.getProcessDefinitionKey() + "-" + record.getMessageName() + "-" + record.getProcessInstanceKey());
                                    newEntity.setMessageName(record.getMessageName());
                                    newEntity.setProcessDefinitionKey(record.getProcessDefinitionKey());
                                    newEntity.setTargetFlowNodeId(record.getStartEventId());
                                    return newEntity;
                                });

        entity.setState(intent.name().toLowerCase());
        entity.setTimestamp(timestamp);
        messageSubscriptionRepository.save(entity);
    }
}
