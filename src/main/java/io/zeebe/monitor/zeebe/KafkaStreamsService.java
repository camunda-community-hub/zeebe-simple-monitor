package io.zeebe.monitor.zeebe;

import io.zeebe.monitor.zeebe.kafka.ZeebeKafkaStreams;
import io.zeebe.monitor.zeebe.kafka.importers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class KafkaStreamsService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private KakaProcessAndElementImporter processAndElementImporter;

    @Autowired
    private KafkaTimerImporter timerImporter;

    @Autowired
    private KafkaVariableImporter variableImporter;

    @Autowired
    private KafkaErrorImporter kafkaErrorImporter;

    @Autowired
    private KafkaMessageImporter kafkaMessageImporter;

    @Autowired
    private KafkaIncidentImporter kafkaIncidentImporter;

    @Autowired
    private ZeebeKafkaStreams zeebeKafkaStreams;

    @PostConstruct
    public void start() {
        zeebeKafkaStreams.kafkaProcessStream().foreach((key, event) -> {
            if (event != null) {
                processAndElementImporter.importProcess(event);
            }
        });

        zeebeKafkaStreams.kafkaProcessInstanceStream().foreach((key, event) -> {
            if (event != null) {
                processAndElementImporter.importProcessInstance(event);
            }
        });

        zeebeKafkaStreams.kafkaZeebeTimerStream().foreach((key, event) -> {
            if (event != null) {
                timerImporter.importTimer(event);
            }
        });

        zeebeKafkaStreams.kafkaZeebeVariableStream().foreach((key, event) -> {
            if (event != null) {
                variableImporter.importVariable(event);
            }
        });

        zeebeKafkaStreams.kafkaZeebeErrorStream().foreach((key, event) -> {
            if (event != null) {
                kafkaErrorImporter.importError(event);
            }
        });

        zeebeKafkaStreams.kafkaZeebeMessagreStream().foreach((key, event) -> {
            if (event != null) {
                kafkaMessageImporter.importMessage(event);
            }
        });

        zeebeKafkaStreams.kafkaZeebeIncidentStream().foreach((key, event) -> {
            if (event != null) {
                kafkaIncidentImporter.importIncident(event);
            }
        });
    }
}
