package io.zeebe.monitor.config;

import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("business-key")
public class BusinessKeyProperties {

  private Set<String> fromVariablesKeys = new HashSet<>();

  public Set<String> getFromVariablesKeys() {
    return fromVariablesKeys;
  }

  public void setFromVariablesKeys(Set<String> fromVariablesKeys) {
    this.fromVariablesKeys = fromVariablesKeys;
  }
}
