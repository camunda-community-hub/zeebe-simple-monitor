package io.zeebe.monitor.zeebe.kafka;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "zeebe-importer", havingValue = "kafka")
@Component
@Timed(
    value = "kafka.messages.handle.time",
    description = "Time taken to handle messages from kafka topic",
    histogram = true)
public class ZeebeKafkaService {

  private static final Logger LOG = LoggerFactory.getLogger(ZeebeKafkaService.class);

  @Autowired private RecordDeserializer deserializer;
  @Autowired private KafkaImportService kafkaImportService;

  @KafkaListener(topics = "${kafka.topics.defaults}", groupId = "${spring.kafka.group-id}")
  public void listener(List<ConsumerRecord<String, byte[]>> messages) {
    try {
      final var records =
          messages.stream()
              .map(deserializer::deserialize)
              .filter(it -> it != null && kafkaImportService.isAvailableType(it.getValueType()))
              .toList();

      if (!records.isEmpty()) {
        logBatch(records);
        kafkaImportService.save(records);
      }
    } catch (Exception e) {
      LOG.error("Error receiving batch from kafka", e);
      throw e;
    }
  }

  private void logBatch(List<Record<RecordValue>> records) {
    var collect =
        records.stream()
            .collect(Collectors.groupingBy(Record::getValueType, Collectors.counting()));
    LOG.debug("=== Got batch of messages from kafka: {}", collect);
  }
}
