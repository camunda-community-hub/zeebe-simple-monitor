package io.zeebe.monitor.rest;

public final class ErrorDto {

  private long position;
  private long errorEventPosition;
  private Long processInstanceKey;
  private String exceptionMessage;
  private String stacktrace;
  private String timestamp;

  public long getPosition() {
    return position;
  }

  public void setPosition(final long position) {
    this.position = position;
  }

  public long getErrorEventPosition() {
    return errorEventPosition;
  }

  public void setErrorEventPosition(final long errorEventPosition) {
    this.errorEventPosition = errorEventPosition;
  }

  public Long getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(final Long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public String getExceptionMessage() {
    return exceptionMessage;
  }

  public void setExceptionMessage(final String exceptionMessage) {
    this.exceptionMessage = exceptionMessage;
  }

  public String getStacktrace() {
    return stacktrace;
  }

  public void setStacktrace(final String stacktrace) {
    this.stacktrace = stacktrace;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(final String timestamp) {
    this.timestamp = timestamp;
  }
}
