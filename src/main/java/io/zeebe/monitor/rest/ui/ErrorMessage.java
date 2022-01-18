package io.zeebe.monitor.rest.ui;

public class ErrorMessage {

  private String message;

  public ErrorMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }
}
