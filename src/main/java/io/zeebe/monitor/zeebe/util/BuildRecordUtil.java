package io.zeebe.monitor.zeebe.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.rest.dto.GenericKafkaRecord;
import org.apache.commons.lang3.math.NumberUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class BuildRecordUtil {
    public static Schema.ProcessRecord buildProcessRecord(GenericKafkaRecord genericKafkaRecord) {
        Map values = genericKafkaRecord.getValue();
        return Schema.ProcessRecord.newBuilder()
                .setVersion(getVersion(values))
                .setBpmnProcessId(getBpmnProcessId(values))
                .setResource(getResource(values))
                .setProcessDefinitionKey(getProcessDefinitionKey(values))
                .setResourceName((String)values.get("resourceName"))
                .setChecksum(getChecksum(values))
                .setMetadata(getMetaData(genericKafkaRecord))
                .build();
    }
    public static Schema.ProcessInstanceRecord buildProcessInstanceRecord(GenericKafkaRecord genericKafkaRecord) {
        Map values = genericKafkaRecord.getValue();
        return Schema.ProcessInstanceRecord.newBuilder()
                .setVersion(getVersion(values))
                .setBpmnProcessId(getBpmnProcessId(values))
                .setProcessInstanceKey(getProcessInstanceKey(values))
                .setProcessDefinitionKey(getProcessDefinitionKey(values))
                .setElementId((String)values.get("elementId"))
                .setFlowScopeKey(getFlowScopeKey(values))
                .setBpmnElementType((String)values.get("bpmnElementType"))
                .setParentProcessInstanceKey(getParentProcessInstanceKey(values))
                .setParentElementInstanceKey(getParentElementInstanceKey(values))
                .setMetadata(getMetaData(genericKafkaRecord))
                .build();
    }

    public static Schema.TimerRecord buildTimerRecord(GenericKafkaRecord genericKafkaRecord) {
        Map values = genericKafkaRecord.getValue();
        return Schema.TimerRecord.newBuilder()
                .setProcessDefinitionKey(getProcessDefinitionKey(values))
                .setProcessInstanceKey(getProcessInstanceKey(values))
                .setElementInstanceKey(getElementInstanceKey(values))
                .setTargetElementId((String)values.get("targetElementId"))
                .setDueDate(getDueDate(values))
                .setRepetitions(getRepetitions(values))
                .setMetadata(getMetaData(genericKafkaRecord))
                .build();
    }

    public static Schema.VariableRecord buildVariableRecord(GenericKafkaRecord genericKafkaRecord) {
        Map values = genericKafkaRecord.getValue();
        return Schema.VariableRecord.newBuilder()
                .setName((String)values.get("name"))
                .setValue((String)values.get("value"))
                .setProcessDefinitionKey(getProcessDefinitionKey(values))
                .setProcessInstanceKey(getProcessInstanceKey(values))
                .setScopeKey(getScopeKey(values))
                .setMetadata(getMetaData(genericKafkaRecord))
                .build();

    }

    public static Schema.ErrorRecord buildErrorRecord(GenericKafkaRecord genericKafkaRecord) {
        Map values = genericKafkaRecord.getValue();
        return Schema.ErrorRecord.newBuilder()
                .setErrorEventPosition(getErrorEventPosition(values))
                .setProcessInstanceKey(getProcessInstanceKey(values))
                .setExceptionMessage((String)values.get("exceptionMessage"))
                .setStacktrace((String)values.get("stacktrace"))
                .setMetadata(getMetaData(genericKafkaRecord))
                .build();
    }

    public static Schema.MessageRecord buildMessageRecord(GenericKafkaRecord genericKafkaRecord) {
        Map values = genericKafkaRecord.getValue();
        return Schema.MessageRecord.newBuilder()
                .setName((String)values.get("name"))
                .setCorrelationKey((String)values.get("correlationKey"))
                .setMessageId((String)values.get("messageId"))
                .setVariables(getVariables(values))
                .setMetadata(getMetaData(genericKafkaRecord))
                .build();
    }

    public static Schema.IncidentRecord buildIncidentRecord(GenericKafkaRecord genericKafkaRecord) {
        Map values = genericKafkaRecord.getValue();
        return Schema.IncidentRecord.newBuilder()
                .setBpmnProcessId(getBpmnProcessId(values))
                .setProcessDefinitionKey(getProcessDefinitionKey(values))
                .setProcessInstanceKey(getProcessInstanceKey(values))
                .setElementInstanceKey(getElementInstanceKey(values))
                .setJobKey(getJobKey(values))
                .setErrorType((String)values.get("errorType"))
                .setErrorMessage((String)values.get("errorMessage"))
                .setMetadata(getMetaData(genericKafkaRecord))
                .build();
    }

    public static Schema.MessageSubscriptionRecord buildMessageSubscriptionRecord(GenericKafkaRecord genericKafkaRecord) {
        Map values = genericKafkaRecord.getValue();
        return Schema.MessageSubscriptionRecord.newBuilder()
                .setBpmnProcessId(getBpmnProcessId(values))
                .setElementInstanceKey(getElementInstanceKey(values))
                .setMessageName((String)values.get("messageName"))
                .setCorrelationKey((String)values.get("correlationKey"))
                .setProcessInstanceKey(getProcessInstanceKey(values))
                .setMetadata(getMetaData(genericKafkaRecord))
                .build();
    }

    public static Schema.MessageStartEventSubscriptionRecord buildMessageStartEventSubscriptionRecord(GenericKafkaRecord genericKafkaRecord) {
        Map values = genericKafkaRecord.getValue();
        return Schema.MessageStartEventSubscriptionRecord.newBuilder()
                .setBpmnProcessId(getBpmnProcessId(values))
                .setMessageName((String)values.get("messageName"))
                .setProcessDefinitionKey(getProcessDefinitionKey(values))
                .setStartEventId((String)values.get("startEventId"))
                .setMetadata(getMetaData(genericKafkaRecord))
                .build();
    }

    public static Schema.JobRecord buildJobRecord(GenericKafkaRecord genericKafkaRecord) {
        Map values = genericKafkaRecord.getValue();
        return Schema.JobRecord.newBuilder()
                .setMetadata(getMetaData(genericKafkaRecord))
                .setBpmnProcessId(getBpmnProcessId(values))
                .setProcessInstanceKey(getProcessInstanceKey(values))
                .setElementInstanceKey(getElementInstanceKey(values))
                .setProcessDefinitionKey(getProcessDefinitionKey(values))
                .setType((String)values.get("type"))
                .setWorker((String)values.get("worker"))
                .setRetries(getRetries(values))
                .build();
    }

    private static Schema.RecordMetadata getMetaData(GenericKafkaRecord genericKafkaRecord) {
        return Schema.RecordMetadata.newBuilder()
                .setPartitionId(genericKafkaRecord.getPartitionId())
                .setKey(genericKafkaRecord.getKey())
                .setTimestamp(genericKafkaRecord.getTimestamp())
                .setIntent(genericKafkaRecord.getIntent())
                .setRejectionType(genericKafkaRecord.getRejectionType())
                .setRejectionReason(genericKafkaRecord.getRejectionReason())
                .setSourceRecordPosition(genericKafkaRecord.getSourceRecordPosition())
                .setValueType(Schema.RecordMetadata.ValueType.valueOf(genericKafkaRecord.getValueType()))
                .setRecordType(Schema.RecordMetadata.RecordType.valueOf(genericKafkaRecord.getRecordType()))
                .setPosition(genericKafkaRecord.getPosition())
                .build();
    }

    private static String getBpmnProcessId(Map values) {
        return (String)values.get("bpmnProcessId");
    }

    private static long getProcessInstanceKey(Map values) {
        return NumberUtils.toLong(values.get("processInstanceKey").toString(), 0);
    }

    private static long getProcessDefinitionKey(Map values) {
        return NumberUtils.toLong(values.get("processDefinitionKey").toString(), 0);
    }

    private static int getVersion(Map values) {
        return NumberUtils.toInt(values.get("version").toString(), 0);
    }

    private static int getRetries(Map values) {
        return NumberUtils.toInt(values.get("retries").toString(), 0);
    }

    private static long getFlowScopeKey(Map values) {
        return NumberUtils.toLong(values.get("flowScopeKey").toString(), 0);
    }

    private static long getScopeKey(Map values) {
        return NumberUtils.toLong(values.get("scopeKey").toString(), 0);
    }

    private static long getParentProcessInstanceKey(Map values) {
        return NumberUtils.toLong(values.get("parentProcessInstanceKey").toString(), 0);
    }

    private static long getParentElementInstanceKey(Map values) {
        return NumberUtils.toLong(values.get("parentElementInstanceKey").toString(), 0);
    }

    private static ByteString getResource(Map values) {
        return  ByteString.copyFromUtf8(new String(Base64.getDecoder().decode((String)values.get("resource")), StandardCharsets.UTF_8));
    }

    private static ByteString getChecksum(Map values) {
        return  ByteString.copyFromUtf8((String)values.get("resource"));
    }

    private static long getElementInstanceKey(Map values) {
        return NumberUtils.toLong(values.get("elementInstanceKey").toString(), 0);
    }

    private static long getDueDate(Map values) {
        return NumberUtils.toLong(values.get("dueDate").toString(), 0);
    }

    private static long getJobKey(Map values) {
        return NumberUtils.toLong(values.get("jobKey").toString(), 0);
    }

    private static int getRepetitions(Map values) {
        return NumberUtils.toInt(values.get("repetitions").toString(), 0);
    }

    private static long getErrorEventPosition(Map values) {
        return NumberUtils.toLong(values.get("errorEventPosition").toString(), 0);
    }

    private static Struct getVariables(Map values) {
        return Struct.newBuilder().putFields("variables", Value.newBuilder().setStringValue((String)values.get("variables")).build()).build();
    }
}
