package io.zeebe.monitor.zeebe.hazelcast;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("zeebe.client.worker.hazelcast")
public class HazelcastProperties {
  
  private boolean enabled = true;
  private String connection = "localhost:5701";
  private String connectionTimeout = "PT30S";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getConnection() {
    return connection;
  }

  public void setConnection(String connection) {
    this.connection = connection;
  }

  public String getConnectionTimeout() {
    return connectionTimeout;
  }

  public void setConnectionTimeout(String connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  
}