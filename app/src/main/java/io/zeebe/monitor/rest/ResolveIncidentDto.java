package io.zeebe.monitor.rest;

public class ResolveIncidentDto {

  private long incidentKey;

  private Long jobKey;
  private int remainingRetries;

  private long elementInstanceKey;
  private String payload;

  public long getIncidentKey() {
    return incidentKey;
  }

  public void setIncidentKey(long incidentKey) {
    this.incidentKey = incidentKey;
  }

  public Long getJobKey() {
    return jobKey;
  }

  public void setJobKey(Long jobKey) {
    this.jobKey = jobKey;
  }

  public int getRemainingRetries() {
    return remainingRetries;
  }

  public void setRemainingRetries(int remainingRetries) {
    this.remainingRetries = remainingRetries;
  }

  public long getElementInstanceKey() {
    return elementInstanceKey;
  }

  public void setElementInstanceKey(long elementInstanceKey) {
    this.elementInstanceKey = elementInstanceKey;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }
}
