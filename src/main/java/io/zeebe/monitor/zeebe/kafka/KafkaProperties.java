package io.zeebe.monitor.zeebe.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("zeebe.client.worker.kafka")
public class KafkaProperties {
  
  private boolean enabled = false;

  private Properties consumerProperties;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Properties getConsumerProperties() {
    return consumerProperties;
  }

  public void setConsumerProperties(Properties consumerProperties) {
    this.consumerProperties = consumerProperties;
  }

  
}