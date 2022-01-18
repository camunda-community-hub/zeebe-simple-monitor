package io.zeebe.monitor.rest.ui;

public class ProcessInstanceNotification {

  private long processInstanceKey;
  private long processDefinitionKey;

  private Type type;

  public enum Type {
    CREATED,
    UPDATED,
    REMOVED
  }

  public long getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(final long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public long getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(final long processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public Type getType() {
    return type;
  }

  public void setType(final Type type) {
    this.type = type;
  }
}
