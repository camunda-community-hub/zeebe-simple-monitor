package io.zeebe.monitor.zeebe.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import org.springframework.kafka.annotation.KafkaListener;

/**
 * Kafka listener form a single zeebe topic
 */
public class KafkaListenerProtobufSource extends AbstractProtobufSource
{
  @KafkaListener(containerFactory = "zeebeListenerContainerFactory", topics = "zeebe")
  public void handleRecord(Message message) throws InvalidProtocolBufferException {
    super.handleRecord(message);
  }
}
