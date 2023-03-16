package io.zeebe.monitor.zeebe;

import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.zeebe.importers.*;
import io.zeebe.monitor.zeebe.util.BuildRecordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static io.zeebe.monitor.zeebe.util.ImportUtil.ifEvent;
import static io.zeebe.monitor.zeebe.util.ImportUtil.isEvent;

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

        zeebeKafkaStreams
            .kafkaProcessStream()
            .mapValues((key, event) -> BuildRecordUtil.buildProcessRecord(event))
            .filter((key, record) -> ifEvent(record, Schema.ProcessRecord::getMetadata))
            .foreach((key, record) -> processAndElementImporter.importProcess(record));

        zeebeKafkaStreams.kafkaProcessInstanceStream()
            .mapValues((key, event) -> BuildRecordUtil.buildProcessInstanceRecord(event))
            .filter((key, record) -> ifEvent(record, Schema.ProcessInstanceRecord::getMetadata))
            .foreach((key, record) -> processAndElementImporter.importProcessInstance(record));

        zeebeKafkaStreams.kafkaZeebeTimerStream()
            .mapValues((key, event) -> BuildRecordUtil.buildTimerRecord(event))
            .filter((key, record) -> ifEvent(record, Schema.TimerRecord::getMetadata))
            .foreach((key, record) -> timerImporter.importTimer(record));

        zeebeKafkaStreams.kafkaZeebeVariableStream()
            .mapValues((key, event) -> BuildRecordUtil.buildVariableRecord(event))
            .filter((key, record) -> ifEvent(record, Schema.VariableRecord::getMetadata))
            .foreach((key, record) -> variableImporter.importVariable(record));

        zeebeKafkaStreams.kafkaZeebeErrorStream()
            .mapValues((key, event) -> BuildRecordUtil.buildErrorRecord(event))
            .filter((key, record) -> ifEvent(record, Schema.ErrorRecord::getMetadata))
            .foreach((key, record) -> errorImporter.importError(record));

        zeebeKafkaStreams.kafkaZeebeMessageStream()
            .mapValues((key, event) -> BuildRecordUtil.buildMessageRecord(event))
            .filter((key, record) -> ifEvent(record, Schema.MessageRecord::getMetadata))
            .foreach((key, record) -> messageImporter.importMessage(record));

        zeebeKafkaStreams.kafkaZeebeIncidentStream()
            .mapValues((key, event) -> BuildRecordUtil.buildIncidentRecord(event))
            .filter((key, record) -> ifEvent(record, Schema.IncidentRecord::getMetadata))
            .foreach((key, record) -> incidentImporter.importIncident(record));

        zeebeKafkaStreams.kafkaZeebeMessageSubscriptionStream()
            .mapValues((key, event) -> BuildRecordUtil.buildMessageSubscriptionRecord(event))
            .filter((key, record) -> ifEvent(record, Schema.MessageSubscriptionRecord::getMetadata))
            .foreach((key, record) -> messageSubscriptionImporter.importMessageSubscription(record));

        zeebeKafkaStreams.kafkaZeebeMessageSubscriptionStartEventStream()
            .mapValues((key, event) -> BuildRecordUtil.buildMessageStartEventSubscriptionRecord(event))
            .filter((key, record) -> ifEvent(record, Schema.MessageStartEventSubscriptionRecord::getMetadata))
            .foreach((key, record) -> messageSubscriptionImporter.importMessageStartEventSubscription(record));

        zeebeKafkaStreams.kafkaZeebeJobStream()
            .mapValues((key, event) -> BuildRecordUtil.buildJobRecord(event))
            .filter((key, record) -> ifEvent(record, Schema.JobRecord::getMetadata))
            .foreach((key, record) -> jobImporter.importJob(record));
    }
}
