package io.zeebe.monitor.rest;

public class MessageSubscriptionDto {

  private String key = "";

  private String messageName = "";
  private String correlationKey = "";

  private String elementId = "";
  private Long elementInstanceKey;
  private Long workflowInstanceKey;

  private String state;
  private String timestamp = "";

  private boolean isOpen;

  public String getMessageName() {
    return messageName;
  }

  public void setMessageName(String messageName) {
    this.messageName = messageName;
  }

  public String getCorrelationKey() {
    return correlationKey;
  }

  public void setCorrelationKey(String correlationKey) {
    this.correlationKey = correlationKey;
  }

  public String getElementId() {
    return elementId;
  }

  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public boolean isOpen() {
    return isOpen;
  }

  public void setOpen(boolean isOpen) {
    this.isOpen = isOpen;
  }

  public Long getElementInstanceKey() {
    return elementInstanceKey;
  }

  public void setElementInstanceKey(Long elementInstanceKey) {
    this.elementInstanceKey = elementInstanceKey;
  }

  public Long getWorkflowInstanceKey() {
    return workflowInstanceKey;
  }

  public void setWorkflowInstanceKey(Long workflowInstanceKey) {
    this.workflowInstanceKey = workflowInstanceKey;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
}
