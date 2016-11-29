package com.camunda.consulting.tngp.dto;

import java.util.ArrayList;
import java.util.List;

import org.camunda.tngp.client.event.impl.dto.WorkflowDefinitionEventImpl;
import org.camunda.tngp.protocol.log.BpmnProcessEventDecoder;
import org.camunda.tngp.protocol.log.WorkflowInstanceRequestDecoder;

public class WorkflowInstanceDto {

  private String broker;
  private long id;
  private long workflowDefinitionId;
  private String workflowDefinitionKey;

  private String payload;
  private List<String> runningActivities = new ArrayList<String>();
  private List<String> endedActivities = new ArrayList<String>();
    
  public String getBroker() {
    return broker;
  }

  public void setBroker(String broker) {
    this.broker = broker;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public long getId() {
    return id;
  }

  public long getWorkflowDefinitionId() {
    return workflowDefinitionId;
  }

  public String getWorkflowDefinitionKey() {
    return workflowDefinitionKey;
  }

  public void setId(long id) {
    this.id = id;
  }

  public void setWorkflowDefinitionId(long workflowDefinitionId) {
    this.workflowDefinitionId = workflowDefinitionId;
  }

  public void setWorkflowDefinitionKey(String workflowDefinitionKey) {
    this.workflowDefinitionKey = workflowDefinitionKey;
  }

  public List<String> getRunningActivities() {
    return runningActivities;
  }

  public void setRunningActivities(List<String> runningActivities) {
    this.runningActivities = runningActivities;
  }

  public List<String> getEndedActivities() {
    return endedActivities;
  }

  public void setEndedActivities(List<String> endedActivities) {
    this.endedActivities = endedActivities;
  }


  @Override
  public String toString() {
    return "WorkflowInstanceDto [id=" + id + ", workflowDefinitionId=" + workflowDefinitionId + ", workflowDefinitionKey=" + workflowDefinitionKey + ", broker="
        + broker + ", runningActivities=" + runningActivities + ", endedActivities=" + endedActivities + ", payload=" + payload + "]";
  }

 


}
