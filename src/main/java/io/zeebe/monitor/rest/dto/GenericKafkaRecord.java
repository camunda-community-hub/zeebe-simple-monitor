package io.zeebe.monitor.rest.dto;

import java.util.Map;

public class GenericKafkaRecord {
    private int partitionId;
    private Map value;
    private long key;
    private long timestamp;
    private String intent;
    private String rejectionType;

    private String rejectionReason;
    private int sourceRecordPosition;
    private String valueType;
    private String brokerVersion;
    private String recordType;
    private long position;

    public int getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(int partitionId) {
        this.partitionId = partitionId;
    }

    public Map getValue() {
        return value;
    }

    public void setValue(Map value) {
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getRejectionType() {
        return rejectionType;
    }

    public void setRejectionType(String rejectionType) {
        this.rejectionType = rejectionType;
    }

    public int getSourceRecordPosition() {
        return sourceRecordPosition;
    }

    public void setSourceRecordPosition(int sourceRecordPosition) {
        this.sourceRecordPosition = sourceRecordPosition;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getBrokerVersion() {
        return brokerVersion;
    }

    public void setBrokerVersion(String brokerVersion) {
        this.brokerVersion = brokerVersion;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    @Override
    public String toString() {
        return "GenericRecord{" +
                "partitionId='" + partitionId + '\'' +
                ", value=" + value +
                ", key='" + key + '\'' +
                ", timestamp=" + timestamp +
                ", intent='" + intent + '\'' +
                ", rejectionType='" + rejectionType + '\'' +
                ", sourceRecordPosition=" + sourceRecordPosition +
                ", valueType='" + valueType + '\'' +
                ", brokerVersion='" + brokerVersion + '\'' +
                ", recordType='" + recordType + '\'' +
                ", position=" + position +
                '}';
    }
}
