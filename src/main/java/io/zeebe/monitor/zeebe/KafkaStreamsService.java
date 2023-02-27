package io.zeebe.monitor.zeebe;

import io.zeebe.monitor.zeebe.importers.*;
import io.zeebe.monitor.zeebe.util.BuildRecordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Profile("kafka")
@Component
public class KafkaStreamsService {
    @Autowired
    private ProcessAndElementImporter processAndElementImporter;

    @Autowired
    private TimerImporter timerImporter;

    @Autowired
    private VariableImporter variableImporter;

    @Autowired
    private ErrorImporter errorImporter;

    @Autowired
    private MessageImporter messageImporter;

    @Autowired
    private IncidentImporter incidentImporter;

    @Autowired
    private MessageSubscriptionImporter messageSubscriptionImporter;

    @Autowired
    private JobImporter jobImporter;

    @Autowired
    private ZeebeKafkaStreams zeebeKafkaStreams;

    @PostConstruct
    public void start() {
        zeebeKafkaStreams.kafkaProcessStream().foreach((key, event) -> {
            processAndElementImporter.importProcess(BuildRecordUtil.buildProcessRecord(event));
        });

        zeebeKafkaStreams.kafkaProcessInstanceStream().foreach((key, event) -> {
                processAndElementImporter.importProcessInstance(BuildRecordUtil.buildProcessInstanceRecord(event));
        });

        zeebeKafkaStreams.kafkaZeebeTimerStream().foreach((key, event) -> {
                timerImporter.importTimer(BuildRecordUtil.buildTimerRecord(event));
        });

        zeebeKafkaStreams.kafkaZeebeVariableStream().foreach((key, event) -> {
                variableImporter.importVariable(BuildRecordUtil.buildVariableRecord(event));
        });

        zeebeKafkaStreams.kafkaZeebeErrorStream().foreach((key, event) -> {
            errorImporter.importError(BuildRecordUtil.buildErrorRecord(event));
        });

        zeebeKafkaStreams.kafkaZeebeMessagreStream().foreach((key, event) -> {
            messageImporter.importMessage(BuildRecordUtil.buildMessageRecord(event));
        });

        zeebeKafkaStreams.kafkaZeebeIncidentStream().foreach((key, event) -> {
            incidentImporter.importIncident(BuildRecordUtil.buildIncidentRecord(event));
        });

        zeebeKafkaStreams.kafkaZeebeMessageSubscriptionStream().foreach((key, event) -> {
                messageSubscriptionImporter.importMessageSubscription(BuildRecordUtil.buildMessageSubscriptionRecord(event));
        });

        zeebeKafkaStreams.kafkaZeebeMessageSubscriptionStartEventStream().foreach((key, event) -> {
            messageSubscriptionImporter.importMessageStartEventSubscription(BuildRecordUtil.buildMessageStartEventSubscriptionRecord(event));
        });

        zeebeKafkaStreams.kafkaZeebeJobStream().foreach((key, event) -> {
                jobImporter.importJob(BuildRecordUtil.buildJobRecord(event));
        });
    }
}
