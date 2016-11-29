package com.camunda.consulting.tngp.dto;

import org.camunda.tngp.client.event.impl.dto.WorkflowDefinitionEventImpl;

public class WorkflowDefinitionDto {

  private String broker;
  private String resource;
  private String key;
  private long id;

  public static WorkflowDefinitionDto from(WorkflowDefinitionEventImpl workflowDefinitionEvent) {
    WorkflowDefinitionDto dto = new WorkflowDefinitionDto();
    dto.id = workflowDefinitionEvent.getId();
    dto.key = workflowDefinitionEvent.getKey();
    dto.resource = workflowDefinitionEvent.getResource();
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

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "WorkflowDefinitionDto [key=" + key + ", id=" + id + "]";
  }

  public String getBroker() {
    return broker;
  }

  public void setBroker(String broker) {
    this.broker = broker;
  }

}
