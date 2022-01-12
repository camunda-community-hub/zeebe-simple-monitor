package io.zeebe.monitor.rest.dto;

public class TimerDto {

  private String elementId = "";
  private Long elementInstanceKey;

  private String dueDate = "";
  private String repetitions = "";

  private String state;
  private String timestamp = "";

  public String getElementId() {
    return elementId;
  }

  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  public Long getElementInstanceKey() {
    return elementInstanceKey;
  }

  public void setElementInstanceKey(Long elementInstanceKey) {
    this.elementInstanceKey = elementInstanceKey;
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
