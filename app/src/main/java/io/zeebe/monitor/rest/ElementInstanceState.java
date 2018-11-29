package io.zeebe.monitor.rest;

public class ElementInstanceState {

  private String elementId;

  private long activeInstances;
  private long endedInstances;

  public String getElementId() {
    return elementId;
  }

  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  public long getActiveInstances() {
    return activeInstances;
  }

  public void setActiveInstances(long activeInstances) {
    this.activeInstances = activeInstances;
  }

  public long getEndedInstances() {
    return endedInstances;
  }

  public void setEndedInstances(long endedInstances) {
    this.endedInstances = endedInstances;
  }
}
