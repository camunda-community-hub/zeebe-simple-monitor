package io.zeebe.monitor.rest.dto;

public class IncidentListDto {

  private long key;

  private String bpmnProcessId;
  private long processInstanceKey;
  private long processDefinitionKey;

  private String errorType;
  private String errorMessage;

  private String state = "";
  private String createdTime = "";
  private String resolvedTime = "";

  public long getKey() {
    return key;
  }

  public void setKey(final long key) {
    this.key = key;
  }

  public String getErrorType() {
    return errorType;
  }

  public void setErrorType(final String errorType) {
    this.errorType = errorType;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(final String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getState() {
    return state;
  }

  public void setState(final String state) {
    this.state = state;
  }

  public String getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(final String createdTime) {
    this.createdTime = createdTime;
  }

  public String getResolvedTime() {
    return resolvedTime;
  }

  public void setResolvedTime(final String resolvedTime) {
    this.resolvedTime = resolvedTime;
  }

  public String getBpmnProcessId() {
    return bpmnProcessId;
  }

  public void setBpmnProcessId(final String bpmnProcessId) {
    this.bpmnProcessId = bpmnProcessId;
  }

  public long getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(final long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public long getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(final long processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }
}
