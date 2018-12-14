package io.zeebe.monitor.rest;

public class IncidentDto {

  private long key;

  private String activityId;
  private long activityInstanceKey;
  private Long jobKey;

  private String payload = "";

  private String errorType;
  private String errorMessage;

  private String state = "";
  private String createdTime = "";
  private String resolvedTime = "";

  private boolean isResolved;

  public long getKey() {
    return key;
  }

  public void setKey(long key) {
    this.key = key;
  }

  public long getActivityInstanceKey() {
    return activityInstanceKey;
  }

  public void setActivityInstanceKey(long activityInstanceKey) {
    this.activityInstanceKey = activityInstanceKey;
  }

  public Long getJobKey() {
    return jobKey;
  }

  public void setJobKey(Long jobKey) {
    this.jobKey = jobKey;
  }

  public String getErrorType() {
    return errorType;
  }

  public void setErrorType(String errorType) {
    this.errorType = errorType;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  public String getResolvedTime() {
    return resolvedTime;
  }

  public void setResolvedTime(String resolvedTime) {
    this.resolvedTime = resolvedTime;
  }

  public boolean isResolved() {
    return isResolved;
  }

  public void setResolved(boolean isResolved) {
    this.isResolved = isResolved;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }
}
