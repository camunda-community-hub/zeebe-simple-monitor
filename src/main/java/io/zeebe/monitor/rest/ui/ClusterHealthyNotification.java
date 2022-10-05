package io.zeebe.monitor.rest.ui;

public class ClusterHealthyNotification {
  private String healthyString;
  private boolean healthy;

  public ClusterHealthyNotification() {
    // allow empty constructor
  }

  public ClusterHealthyNotification(String healthyString, boolean healthy) {
    this.healthyString = healthyString;
    this.healthy = healthy;
  }

  public String getHealthyString() {
    return healthyString;
  }

  public void setHealthyString(String healthyString) {
    this.healthyString = healthyString;
  }

  public boolean isHealthy() {
    return healthy;
  }

  public void setHealthy(boolean healthy) {
    this.healthy = healthy;
  }
}
