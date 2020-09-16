package io.zeebe.monitor.zeebe.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.protobuf.Message;

import org.apache.kafka.common.serialization.LongDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import io.zeebe.exporter.proto.Schema;
import io.zeebe.exporters.kafka.serde.ProtobufRecordDeserializer;
import io.zeebe.monitor.zeebe.ZeebeImportService;

@Configuration
@EnableKafka
@ConditionalOnProperty(name="zeebe.client.worker.kafka.enabled", havingValue="true")
@EnableConfigurationProperties(value={KafkaProperties.class})
public class KafkaConfiguration {
  
  private static final Logger LOG = LoggerFactory.getLogger(KafkaConfiguration.class);

  @Autowired KafkaProperties kafkaProperties;

  @Bean
  public ConsumerFactory<Long, Message> zeebeConsumerFactory() {
    Properties props = kafkaProperties.getConsumerProperties();
    Map<String, Object> p = new HashMap(props);

    LOG.info("Connecting to Kafka '{}'", props.getProperty("bootstrap.servers"));

    return new DefaultKafkaConsumerFactory<>(p,
        new LongDeserializer(),
        new ProtobufRecordDeserializer());
  }

  @Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Long, Message>> zeebeListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<Long, Message> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(zeebeConsumerFactory());
		return factory;
  }
  
  @Bean 
  public KafkaListenerProtobufSource kafkaListenerProtobufStream(ZeebeImportService zeebeProtobufImportService) {

    KafkaListenerProtobufSource protobufSource = new KafkaListenerProtobufSource();
  
    zeebeProtobufImportService.importFrom(protobufSource);

    return protobufSource;
  }

}
