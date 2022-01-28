package io.zeebe.monitor.rest.dto;

public class CalledProcessInstanceDto {

  private String elementId;
  private long elementInstanceKey;

  private long childProcessInstanceKey;
  private String childBpmnProcessId;

  private String childState;

  public String getElementId() {
    return elementId;
  }

  public void setElementId(final String elementId) {
    this.elementId = elementId;
  }

  public long getElementInstanceKey() {
    return elementInstanceKey;
  }

  public void setElementInstanceKey(final long elementInstanceKey) {
    this.elementInstanceKey = elementInstanceKey;
  }

  public long getChildProcessInstanceKey() {
    return childProcessInstanceKey;
  }

  public void setChildProcessInstanceKey(final long childProcessInstanceKey) {
    this.childProcessInstanceKey = childProcessInstanceKey;
  }

  public String getChildBpmnProcessId() {
    return childBpmnProcessId;
  }

  public void setChildBpmnProcessId(final String childBpmnProcessId) {
    this.childBpmnProcessId = childBpmnProcessId;
  }

  public String getChildState() {
    return childState;
  }

  public void setChildState(final String childState) {
    this.childState = childState;
  }
}
