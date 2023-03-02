package io.zeebe.monitor.zeebe;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zeebe.monitor.rest.dto.GenericKafkaRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("kafka")
@Component
public class ZeebeKafkaStreams {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    public StreamsBuilder streamsBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${kafka.streams.maxMessageSizeBytes:5242880}")
    private int maxMessageSizeBytes;

    @Bean
    public KStream<String, GenericKafkaRecord> kafkaProcessInstanceStream() {
        KStream stream = this.streamsBuilder.stream("zeebe-process-instance", Consumed.with(Serdes.String(), Serdes.String())).mapValues(value -> readValue(value)).filter((key, value) -> value != null);
        this.logger.debug("Initialize zeebe-process-instance kafka stream");

        return stream;
    }

    @Bean
    public KStream<String, GenericKafkaRecord> kafkaProcessStream() {
        KStream stream = this.streamsBuilder.stream("zeebe-process", Consumed.with(Serdes.String(), Serdes.String())).mapValues(value -> readValue(value)).filter((key, value) -> value != null);
        this.logger.debug("Initialize zeebe-process kafka stream");
        return stream;
    }


    @Bean
    public KStream<String, GenericKafkaRecord> kafkaZeebeErrorStream() {
        KStream stream = this.streamsBuilder.stream("zeebe-error", Consumed.with(Serdes.String(), Serdes.String())).mapValues(value -> readValue(value)).filter((key, value) -> value != null);
        this.logger.debug("Initialize zeebe-error kafka stream");
        return stream;
    }

    @Bean
    public KStream<String, GenericKafkaRecord> kafkaZeebeMessageStream() {
        KStream stream = this.streamsBuilder.stream("zeebe-message", Consumed.with(Serdes.String(), Serdes.String())).mapValues(value -> readValue(value)).filter((key, value) -> value != null);
        this.logger.debug("Initialize zeebe-message kafka stream");
        return stream;
    }

    @Bean
    public KStream<String, GenericKafkaRecord> kafkaZeebeIncidentStream() {
        KStream stream = this.streamsBuilder.stream("zeebe-incident", Consumed.with(Serdes.String(), Serdes.String())).mapValues(value -> readValue(value)).filter((key, value) -> value != null);
        this.logger.debug("Initialize zeebe-incident kafka stream");
        return stream;
    }

    @Bean
    public KStream<String, GenericKafkaRecord> kafkaZeebeTimerStream() {
        KStream stream = this.streamsBuilder.stream("zeebe-timer", Consumed.with(Serdes.String(), Serdes.String())).mapValues(value -> readValue(value)).filter((key, value) -> value != null);
        this.logger.debug("Initialize zeebe-timer kafka stream");
        return stream;
    }

    @Bean
    public KStream<String, GenericKafkaRecord> kafkaZeebeVariableStream() {
        KStream stream = this.streamsBuilder.stream("zeebe-variable", Consumed.with(Serdes.String(), Serdes.String())).mapValues(value -> readValue(value)).filter((key, value) -> value != null);
        this.logger.debug("Initialize zeebe-variable kafka stream");
        return stream;
    }

    @Bean
    public KStream<String, GenericKafkaRecord> kafkaZeebeMessageSubscriptionStream() {
        KStream stream = this.streamsBuilder.stream("zeebe-message-subscription", Consumed.with(Serdes.String(), Serdes.String())).mapValues(value -> readValue(value)).filter((key, value) -> value != null);
        this.logger.debug("Initialize zeebe-message-subscription kafka stream");
        return stream;
    }

    @Bean
    public KStream<String, GenericKafkaRecord> kafkaZeebeMessageSubscriptionStartEventStream() {
        KStream stream = this.streamsBuilder.stream("zeebe-message-subscription-start-event", Consumed.with(Serdes.String(), Serdes.String())).mapValues(value -> readValue(value)).filter((key, value) -> value != null);
        this.logger.debug("Initialize zeebe-message-subscription-start-event kafka stream");
        return stream;
    }

    @Bean
    public KStream<String, GenericKafkaRecord> kafkaZeebeJobStream() {
        KStream stream = this.streamsBuilder.stream("zeebe-job", Consumed.with(Serdes.String(), Serdes.String())).mapValues(value -> readValue(value)).filter((key, value) -> value != null);
        this.logger.debug("Initialize zeebe-job kafka stream");
        return stream;
    }

    private GenericKafkaRecord readValue(String value) {
        try {
            return objectMapper.readValue(value, GenericKafkaRecord.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
