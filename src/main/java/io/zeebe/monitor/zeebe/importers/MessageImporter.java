package io.zeebe.monitor.zeebe.importers;

import io.camunda.zeebe.protocol.record.intent.MessageIntent;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.MessageEntity;
import io.zeebe.monitor.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class MessageImporter {

    @Autowired
    private MessageRepository messageRepository;

    private static final Logger LOG = LoggerFactory.getLogger(MessageImporter.class);

    public void importMessage(final Schema.MessageRecord record) {

        final MessageIntent intent = MessageIntent.valueOf(record.getMetadata().getIntent());
        final long key = record.getMetadata().getKey();
        final long timestamp = record.getMetadata().getTimestamp();

        final MessageEntity entity =
                messageRepository
                        .findById(key)
                        .orElseGet(
                                () -> {
                                    final MessageEntity newEntity = new MessageEntity();
                                    newEntity.setKey(key);
                                    newEntity.setName(record.getName());
                                    newEntity.setCorrelationKey(record.getCorrelationKey());
                                    newEntity.setMessageId(record.getMessageId());
                                    newEntity.setPayload(record.getVariables().toString());
                                    return newEntity;
                                });

        entity.setState(intent.name().toLowerCase());
        entity.setTimestamp(timestamp);
        try {
            messageRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            LOG.warn("Attempted to save duplicate Message with key {}", entity.getKey());
        }
    }
}
