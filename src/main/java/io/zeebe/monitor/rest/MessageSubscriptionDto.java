package io.zeebe.monitor.rest;

public class MessageSubscriptionDto {

  private String key = "";

  private String messageName = "";
  private String correlationKey = "";

  private String elementId = "";
  private Long elementInstanceKey;
  private Long processInstanceKey;

  private String state;
  private String timestamp = "";

  private boolean isOpen;

  public String getMessageName() {
    return messageName;
  }

  public void setMessageName(final String messageName) {
    this.messageName = messageName;
  }

  public String getCorrelationKey() {
    return correlationKey;
  }

  public void setCorrelationKey(final String correlationKey) {
    this.correlationKey = correlationKey;
  }

  public String getElementId() {
    return elementId;
  }

  public void setElementId(final String elementId) {
    this.elementId = elementId;
  }

  public String getState() {
    return state;
  }

  public void setState(final String state) {
    this.state = state;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(final String timestamp) {
    this.timestamp = timestamp;
  }

  public boolean isOpen() {
    return isOpen;
  }

  public void setOpen(final boolean isOpen) {
    this.isOpen = isOpen;
  }

  public Long getElementInstanceKey() {
    return elementInstanceKey;
  }

  public void setElementInstanceKey(final Long elementInstanceKey) {
    this.elementInstanceKey = elementInstanceKey;
  }

  public Long getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(final Long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }
}
