package io.zeebe.monitor.zeebe.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ZeebeKafkaStreams {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    public StreamsBuilder kStreamBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${kafka.streams.maxMessageSizeBytes:5242880}")
    private int maxMessageSizeBytes;

    @Bean
    public KStream<String, GenericRecord> kafkaProcessInstanceStream() {
        KStream stream = this.kStreamBuilder.stream("zeebe-process-instance", Consumed.with(Serdes.String(), Serdes.String())).mapValues (value -> readValue(value));
        this.logger.debug("Initialize zeebe-process-instance kafka stream");
        return stream;
    }

    @Bean
    public KStream<String, GenericRecord> kafkaProcessStream() {
        KStream stream = this.kStreamBuilder.stream("zeebe-process", Consumed.with(Serdes.String(), Serdes.String())).mapValues (value -> readValue(value));
        this.logger.debug("Initialize zeebe-process kafka stream");
        return stream;
    }


    @Bean
    public KStream<String, GenericRecord> kafkaZeebeErrorStream() {
        KStream stream = this.kStreamBuilder.stream("zeebe-error", Consumed.with(Serdes.String(), Serdes.String())).mapValues (value -> readValue(value));
        this.logger.debug("Initialize zeebe-error kafka stream");
        return stream;
    }

    @Bean
    public KStream<String, GenericRecord> kafkaZeebeMessagreStream() {
        KStream stream = this.kStreamBuilder.stream("zeebe-message", Consumed.with(Serdes.String(), Serdes.String())).mapValues (value -> readValue(value));
        this.logger.debug("Initialize zeebe-message kafka stream");
        return stream;
    }

    @Bean
    public KStream<String, GenericRecord> kafkaZeebeIncidentStream() {
        KStream stream = this.kStreamBuilder.stream("zeebe-incident", Consumed.with(Serdes.String(), Serdes.String())).mapValues (value -> readValue(value));
        this.logger.debug("Initialize zeebe-incident kafka stream");
        return stream;
    }

    @Bean
    public KStream<String, GenericRecord> kafkaZeebeTimerStream() {
        KStream stream = this.kStreamBuilder.stream("zeebe-timer", Consumed.with(Serdes.String(), Serdes.String())).mapValues (value -> readValue(value));
        this.logger.debug("Initialize zeebe-timer kafka stream");
        return stream;
    }

    @Bean
    public KStream<String, GenericRecord> kafkaZeebeVariableStream() {
        KStream stream = this.kStreamBuilder.stream("zeebe-variable", Consumed.with(Serdes.String(), Serdes.String())).mapValues (value -> readValue(value));
        this.logger.debug("Initialize zeebe-variable kafka stream");
        return stream;
    }

    private GenericRecord readValue(String value) {
        try {
            return objectMapper.readValue(value, GenericRecord.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
