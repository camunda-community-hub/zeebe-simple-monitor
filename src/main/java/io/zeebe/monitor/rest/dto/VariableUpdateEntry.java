package io.zeebe.monitor.rest.dto;

public class VariableUpdateEntry {

  private String value;
  private String timestamp;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }
}
