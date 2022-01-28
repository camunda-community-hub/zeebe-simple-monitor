package io.zeebe.monitor.rest.dto;

public class ResolveIncidentDto {

  private long incidentKey;

  private Long jobKey;
  private int remainingRetries;

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
}
