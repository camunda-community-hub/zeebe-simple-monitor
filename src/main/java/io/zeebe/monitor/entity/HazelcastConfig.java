package io.zeebe.monitor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public final class HazelcastConfig {

  @Id private String id;
  private long sequence;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public long getSequence() {
    return sequence;
  }

  public void setSequence(long sequence) {
    this.sequence = sequence;
  }
}
