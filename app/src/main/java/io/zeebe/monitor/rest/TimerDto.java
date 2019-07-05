package io.zeebe.monitor.rest;

public class TimerDto {

  private String activityId = "";
  private Long activityInstanceKey;

  private String dueDate = "";
  private String repetitions = "";

  private String state;
  private String timestamp = "";

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public Long getActivityInstanceKey() {
    return activityInstanceKey;
  }

  public void setActivityInstanceKey(Long activityInstanceKey) {
    this.activityInstanceKey = activityInstanceKey;
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

  public String getDueDate() {
    return dueDate;
  }

  public void setDueDate(String dueDate) {
    this.dueDate = dueDate;
  }

  public String getRepetitions() {
    return repetitions;
  }

  public void setRepetitions(String repetitions) {
    this.repetitions = repetitions;
  }
}
