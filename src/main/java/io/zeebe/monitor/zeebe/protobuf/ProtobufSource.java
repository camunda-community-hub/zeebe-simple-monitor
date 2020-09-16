package io.zeebe.monitor.zeebe.protobuf;
import java.util.function.Consumer;

import io.zeebe.exporter.proto.Schema;

public interface ProtobufSource {
  public void addDeploymentListener(Consumer<Schema.DeploymentRecord> listener);
  public void addWorkflowInstanceListener(Consumer<Schema.WorkflowInstanceRecord> listener);
  public void addVariableListener(Consumer<Schema.VariableRecord> listener);
  public void addVariableDocumentListener(Consumer<Schema.VariableDocumentRecord> listener);
  public void addJobListener(Consumer<Schema.JobRecord> listener);
  public void addJobBatchListener(Consumer<Schema.JobBatchRecord> listener);
  public void addIncidentListener(Consumer<Schema.IncidentRecord> listener);
  public void addTimerListener(Consumer<Schema.TimerRecord> listener);
  public void addMessageListener(Consumer<Schema.MessageRecord> listener);
  public void addMessageSubscriptionListener(Consumer<Schema.MessageSubscriptionRecord> listener);
  public void addMessageStartEventSubscriptionListener(Consumer<Schema.MessageStartEventSubscriptionRecord> listener);
  public void addWorkflowInstanceSubscriptionListener(Consumer<Schema.WorkflowInstanceSubscriptionRecord> listener);
  public void addWorkflowInstanceCreationListener(Consumer<Schema.WorkflowInstanceCreationRecord> listener);
  public void addWorkflowInstanceResultListener(Consumer<Schema.WorkflowInstanceResultRecord> listener);
  public void addErrorListener(Consumer<Schema.ErrorRecord> listener);
}
