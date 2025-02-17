package io.zeebe.monitor.entity;

import jakarta.persistence.*;

@Entity(name = "PROCESS_INSTANCE_BUSINESS_KEY")
@Table(
    indexes = {
      @Index(name = "process_instance_business_key__businessKey", columnList = "BUSINESS_KEY_"),
    })
public class ProcessInstanceBusinessKeyEntity {

  @Id
  @Column(name = "INSTANCE_KEY_")
  private long instanceKey;

  @Column(nullable = false, columnDefinition = "text", name = "BUSINESS_KEY_")
  private String businessKey;

  public long getInstanceKey() {
    return instanceKey;
  }

  public void setInstanceKey(long instanceKey) {
    this.instanceKey = instanceKey;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }
}
