package io.zeebe.monitor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public final class ElasticConfig {

  @Id private String id;
  private long timestamp;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
