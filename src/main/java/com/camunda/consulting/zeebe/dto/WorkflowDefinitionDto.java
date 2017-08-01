package com.camunda.consulting.zeebe.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.json.JsonObject;

public class WorkflowDefinitionDto {

  private String broker;
  private String resource;
  
  /**
   * Generate random uuid for unique identification of workflow definition
   */
  private String uuid = UUID.randomUUID().toString();
  
  private String key;
  private int version;
  
  private long countRunning;
  private long countEnded;

  public static List<WorkflowDefinitionDto> from(JsonObject jsonEvent) {
    ArrayList<WorkflowDefinitionDto> result = new ArrayList<WorkflowDefinitionDto>();
    
    for (int i = 0; i < jsonEvent.getJsonArray("deployedWorkflows").size(); i++) {
      JsonObject workflowJson = jsonEvent.getJsonArray("deployedWorkflows").getJsonObject(i);
      WorkflowDefinitionDto dto = new WorkflowDefinitionDto();
      
      dto.setVersion(workflowJson.getInt("version"));
      dto.setKey(workflowJson.getString("bpmnProcessId"));
      dto.setResource(jsonEvent.getString("bpmnXml"));
      result.add(dto);
    }
    
    return result;    
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


  public String getBroker() {
    return broker;
  }

  public void setBroker(String broker) {
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
    return "WorkflowDefinitionDto [key=" + key + ", broker=" + broker + ", version=" + version + ", countRunning=" + countRunning + ", countEnded=" + countEnded + "]";
  }

  public String getUuid() {
    return uuid;
  }

}
