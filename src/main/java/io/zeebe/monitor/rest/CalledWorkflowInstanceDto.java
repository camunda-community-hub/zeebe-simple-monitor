package io.zeebe.monitor.rest;

public class CalledWorkflowInstanceDto {

  private String elementId;
  private long elementInstanceKey;

  private long childWorkflowInstanceKey;
  private String childBpmnProcessId;

  private String childState;

  public String getElementId() {
    return elementId;
  }

  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  public long getElementInstanceKey() {
    return elementInstanceKey;
  }

  public void setElementInstanceKey(long elementInstanceKey) {
    this.elementInstanceKey = elementInstanceKey;
  }

  public long getChildWorkflowInstanceKey() {
    return childWorkflowInstanceKey;
  }

  public void setChildWorkflowInstanceKey(long childWorkflowInstanceKey) {
    this.childWorkflowInstanceKey = childWorkflowInstanceKey;
  }

  public String getChildBpmnProcessId() {
    return childBpmnProcessId;
  }

  public void setChildBpmnProcessId(String childBpmnProcessId) {
    this.childBpmnProcessId = childBpmnProcessId;
  }

  public String getChildState() {
    return childState;
  }

  public void setChildState(String childState) {
    this.childState = childState;
  }
}
