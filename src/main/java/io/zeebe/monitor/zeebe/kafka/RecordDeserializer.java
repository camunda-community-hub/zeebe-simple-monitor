package io.zeebe.monitor.zeebe.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;
import java.io.IOException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A {@link Deserializer} implementations for {@link Record} objects, which uses a pre-configured
 * {@link ObjectReader} for that type
 */
@Component
public final class RecordDeserializer {

  private static final Logger LOG = LoggerFactory.getLogger(RecordDeserializer.class);

  @Autowired private ObjectMapper objectMapper;

  public <T extends RecordValue> Record<T> deserialize(ConsumerRecord<String, byte[]> record) {
    final String topic = record.topic();
    final byte[] data = record.value();
    try {
      return objectMapper.readValue(data, new TypeReference<>() {});
    } catch (final IOException e) {
      LOG.error(
          String.format("Expected to deserialize record from topic [%s], but failed", topic), e);
      return null;
    }
  }
}
