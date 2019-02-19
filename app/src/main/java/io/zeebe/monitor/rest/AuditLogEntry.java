package io.zeebe.monitor.rest;

public class AuditLogEntry {

  private long key;
  private long flowScopeKey;

  private String elementId;

  private String paylaod;

  private String state;
  private String timestamp;

  public long getKey() {
    return key;
  }

  public void setKey(long key) {
    this.key = key;
  }

  public long getFlowScopeKey() {
    return flowScopeKey;
  }

  public void setFlowScopeKey(long flowScopeKey) {
    this.flowScopeKey = flowScopeKey;
  }

  public String getElementId() {
    return elementId;
  }

  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  public String getPaylaod() {
    return paylaod;
  }

  public void setPaylaod(String paylaod) {
    this.paylaod = paylaod;
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
}
