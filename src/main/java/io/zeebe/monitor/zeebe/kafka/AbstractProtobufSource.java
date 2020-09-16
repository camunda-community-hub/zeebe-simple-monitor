package io.zeebe.monitor.zeebe.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.zeebe.protobuf.ProtobufSource;

public abstract class AbstractProtobufSource implements ProtobufSource {

  private final Map<Class<?>, List<Consumer<?>>> listeners = new HashMap<>();

  public void handleRecord(Message message) throws InvalidProtocolBufferException {
    listeners.entrySet().forEach(entry->{
      if (entry.getKey().isAssignableFrom(message.getClass())) {
        entry.getValue().forEach(listener -> ((Consumer) listener).accept(message));
      }
    });
  }

  private <T extends com.google.protobuf.Message> void addListener(
      Class<T> recordType, Consumer<T> listener) {
      final List<Consumer<?>> recordListeners = listeners.getOrDefault(recordType, new ArrayList<>());
      recordListeners.add(listener);
      listeners.put(recordType, recordListeners);
  }

  public void addDeploymentListener(Consumer<Schema.DeploymentRecord> listener) {
    addListener(Schema.DeploymentRecord.class, listener);
  }

  public void addWorkflowInstanceListener(Consumer<Schema.WorkflowInstanceRecord> listener) {
    addListener(Schema.WorkflowInstanceRecord.class, listener);
  }

  public void addVariableListener(Consumer<Schema.VariableRecord> listener) {
    addListener(Schema.VariableRecord.class, listener);
  }

  public void addVariableDocumentListener(Consumer<Schema.VariableDocumentRecord> listener) {
    addListener(Schema.VariableDocumentRecord.class, listener);
  }

  public void addJobListener(Consumer<Schema.JobRecord> listener) {
    addListener(Schema.JobRecord.class, listener);
  }

  public void addJobBatchListener(Consumer<Schema.JobBatchRecord> listener) {
    addListener(Schema.JobBatchRecord.class, listener);
  }

  public void addIncidentListener(Consumer<Schema.IncidentRecord> listener) {
    addListener(Schema.IncidentRecord.class, listener);
  }

  public void addTimerListener(Consumer<Schema.TimerRecord> listener) {
    addListener(Schema.TimerRecord.class, listener);
  }

  public void addMessageListener(Consumer<Schema.MessageRecord> listener) {
    addListener(Schema.MessageRecord.class, listener);
  }

  public void addMessageSubscriptionListener(
      Consumer<Schema.MessageSubscriptionRecord> listener) {
    addListener(Schema.MessageSubscriptionRecord.class, listener);
  }

  public void addMessageStartEventSubscriptionListener(
      Consumer<Schema.MessageStartEventSubscriptionRecord> listener) {
    addListener(Schema.MessageStartEventSubscriptionRecord.class, listener);
  }

  public void addWorkflowInstanceSubscriptionListener(Consumer<Schema.WorkflowInstanceSubscriptionRecord> listener) {
    addListener(Schema.WorkflowInstanceSubscriptionRecord.class, listener);
  }

  public void addWorkflowInstanceCreationListener(Consumer<Schema.WorkflowInstanceCreationRecord> listener) {
    addListener(Schema.WorkflowInstanceCreationRecord.class, listener);
  }

  public void addWorkflowInstanceResultListener(Consumer<Schema.WorkflowInstanceResultRecord> listener) {
    addListener(Schema.WorkflowInstanceResultRecord.class, listener);
  }

  public void addErrorListener(Consumer<Schema.ErrorRecord> listener) {
    addListener(Schema.ErrorRecord.class, listener);
  }
}
