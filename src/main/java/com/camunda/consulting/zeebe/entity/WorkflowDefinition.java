package com.camunda.consulting.zeebe.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import io.zeebe.client.event.WorkflowEvent;

@Entity
public class WorkflowDefinition {

  /**
   * Generate random uuid for unique identification of workflow definition
   */
  @Id
  private String uuid = UUID.randomUUID().toString();

  private String key;
  private int version;

  @OneToOne
  private Broker broker;

  @Column(length = 100000)
  private String resource;

  @Transient
  private long countRunning;
  
  @Transient
  private long countEnded;

  public static WorkflowDefinition from(WorkflowEvent event) {
    WorkflowDefinition dto = new WorkflowDefinition();

    dto.setVersion(event.getVersion());
    dto.setKey(event.getBpmnProcessId());
    dto.setResource(event.getBpmnXml());
    
    return dto;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public Broker getBroker() {
    return broker;
  }

  public void setBroker(Broker broker) {
    this.broker = broker;
  }

  public long getCountRunning() {
    return countRunning;
  }

  public void setCountRunning(long countRunning) {
    this.countRunning = countRunning;
  }

  public long getCountEnded() {
    return countEnded;
  }

  public void setCountEnded(long countEnded) {
    this.countEnded = countEnded;
  }

  @Override
  public String toString() {
    return "WorkflowDefinitionDto [key=" + key + ", broker=" + broker + ", version=" + version + ", countRunning=" + countRunning + ", countEnded=" + countEnded
        + "]";
  }

  public String getUuid() {
    return uuid;
  }

}
