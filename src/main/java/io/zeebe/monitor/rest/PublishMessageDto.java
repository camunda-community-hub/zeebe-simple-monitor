package io.zeebe.monitor.rest;

public class PublishMessageDto {

  private String name;
  private String correlationKey;
  private String payload;
  private String timeToLive;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCorrelationKey() {
    return correlationKey;
  }

  public void setCorrelationKey(String correlationKey) {
    this.correlationKey = correlationKey;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public String getTimeToLive() {
    return timeToLive;
  }

  public void setTimeToLive(String timeToLive) {
    this.timeToLive = timeToLive;
  }
}
