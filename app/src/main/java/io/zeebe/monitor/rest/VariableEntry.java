package io.zeebe.monitor.rest;

import java.util.ArrayList;
import java.util.List;

public class VariableEntry {

  private String name;
  private String value;
  private long scopeKey;
  private String timestamp;

  private List<VariableUpdateEntry> updates = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public long getScopeKey() {
    return scopeKey;
  }

  public void setScopeKey(long scopeKey) {
    this.scopeKey = scopeKey;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public List<VariableUpdateEntry> getUpdates() {
    return updates;
  }

  public void setUpdates(List<VariableUpdateEntry> updates) {
    this.updates = updates;
  }
}
