package io.zeebe.monitor.rest;

public final class ErrorDto {

  private long position;
  private long errorEventPosition;
  private Long workflowInstanceKey;
  private String exceptionMessage;
  private String stacktrace;
  private String timestamp;

  public long getPosition() {
    return position;
  }

  public void setPosition(long position) {
    this.position = position;
  }

  public long getErrorEventPosition() {
    return errorEventPosition;
  }

  public void setErrorEventPosition(long errorEventPosition) {
    this.errorEventPosition = errorEventPosition;
  }

  public Long getWorkflowInstanceKey() {
    return workflowInstanceKey;
  }

  public void setWorkflowInstanceKey(Long workflowInstanceKey) {
    this.workflowInstanceKey = workflowInstanceKey;
  }

  public String getExceptionMessage() {
    return exceptionMessage;
  }

  public void setExceptionMessage(String exceptionMessage) {
    this.exceptionMessage = exceptionMessage;
  }

  public String getStacktrace() {
    return stacktrace;
  }

  public void setStacktrace(String stacktrace) {
    this.stacktrace = stacktrace;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }
}
