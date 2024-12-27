package io.zeebe.monitor.config;

import com.fasterxml.jackson.core.JacksonException;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@ConditionalOnProperty(name = "zeebe-importer", havingValue = "kafka")
@EnableKafka
@Configuration
public class KafkaConfig {

  private static final Logger LOG = LoggerFactory.getLogger(KafkaConfig.class);

  @Value("${spring.kafka.custom.retry.interval-ms}")
  private Long interval;

  @Value("${spring.kafka.custom.retry.max-attempts}")
  private Long maxAttempts;

  @Value("${spring.kafka.custom.concurrency}")
  private Integer concurrency;

  @Bean
  @Primary
  public <M> ConcurrentKafkaListenerContainerFactory<String, M> kafkaListenerContainerFactory(
      KafkaProperties kafkaProperties, MeterRegistry meterRegistry) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, M>();
    factory.setConsumerFactory(consumerFactory(kafkaProperties, meterRegistry));
    factory.setConcurrency(concurrency);
    factory.setBatchListener(true);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);

    // Any exception thrown from record of the batch will skip(NotRetryableExceptions) or retry the
    // entire batch
    var errorHandler =
        new DefaultErrorHandler(
            (record, e) ->
                LOG.error(
                    String.format(
                        "====== Stop retrying failed message (key=%s) ======", record.key()),
                    e),
            new FixedBackOff(interval, maxAttempts));
    errorHandler.addNotRetryableExceptions(JacksonException.class);
    factory.setCommonErrorHandler(errorHandler);
    return factory;
  }

  @Bean
  public <M> ConsumerFactory<String, M> consumerFactory(
      KafkaProperties kafkaProperties, MeterRegistry meterRegistry) {
    var props = kafkaProperties.buildConsumerProperties(null);
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
    var factory = new DefaultKafkaConsumerFactory<String, M>(props);
    factory.addListener(new MicrometerConsumerListener<>(meterRegistry));
    return factory;
  }
}
