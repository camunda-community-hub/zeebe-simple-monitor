package io.zeebe.monitor.rest.ui;

public class ZeebeClusterNotification {

  private Type type;
  private String message;

  public enum Type {
    INFORMATION,
    SUCCESS,
    ERROR
  }

  public Type getType() {
    return type;
  }

  public void setType(final Type type) {
    this.type = type;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }
}
