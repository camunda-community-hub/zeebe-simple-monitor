package io.zeebe.monitor.rest;

public class WorkflowInstanceNotification {

  private long workflowInstanceKey;
  private long workflowKey;

  private Type type;

  public enum Type {
    CREATED,
    UPDATED,
    REMOVED
  }

  public long getWorkflowInstanceKey() {
    return workflowInstanceKey;
  }

  public void setWorkflowInstanceKey(long workflowInstanceKey) {
    this.workflowInstanceKey = workflowInstanceKey;
  }

  public long getWorkflowKey() {
    return workflowKey;
  }

  public void setWorkflowKey(long workflowKey) {
    this.workflowKey = workflowKey;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }
}
