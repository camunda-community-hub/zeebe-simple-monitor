package com.camunda.consulting.tngp.dto;

import java.util.ArrayList;
import java.util.List;

import org.camunda.tngp.client.event.impl.dto.WorkflowDefinitionEventImpl;
import org.camunda.tngp.protocol.log.WorkflowInstanceRequestDecoder;

public class WorkflowInstanceDto {

  private String broker;
  private long id;
  private long workflowDefinitionId;
  private String workflowDefinitionKey;

  private String payload;
  private List<String> activities = new ArrayList<String>();
//  private List<String> endedActivities = new ArrayList<String>();
  
  public static WorkflowInstanceDto from(WorkflowInstanceRequestDecoder decoder) {
    WorkflowInstanceDto dto = new WorkflowInstanceDto();
    
    dto.id = decoder.wfInstanceId();
//    dto.workflowDefinitionId = decoder.wfDefinitionId();
//    dto.workflowDefinitionKey = decoder.wfDefinitionKey();
    dto.workflowDefinitionId = 1;
    dto.workflowDefinitionKey = "simple";
    dto.payload = decoder.payload();    

    return dto;
  }
  
  public void addActivity(String flowElementIdString, String payload2) {
    activities.add(flowElementIdString);
  }
  
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

  public List<String> getActivities() {
    return activities;
  }


  public long getWorkflowDefinitionId() {
    return workflowDefinitionId;
  }

  public String getWorkflowDefinitionKey() {
    return workflowDefinitionKey;
  }

 


}
