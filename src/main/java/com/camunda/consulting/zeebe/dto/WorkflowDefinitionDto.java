package com.camunda.consulting.zeebe.dto;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.zeebe.client.event.WorkflowDefinition;
import io.zeebe.client.event.WorkflowEvent;
import io.zeebe.client.workflow.impl.DeploymentEventImpl;

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


  public static WorkflowDefinitionDto from(WorkflowEvent event) {
    WorkflowDefinitionDto dto = new WorkflowDefinitionDto();

    dto.setVersion(event.getVersion());
    dto.setKey(event.getBpmnProcessId());
    dto.setResource(event.getBpmnXml());
    
    return dto;
  }

  public static List<WorkflowDefinitionDto> from(DeploymentEventImpl deploymentEvent) {
    ArrayList<WorkflowDefinitionDto> result = new ArrayList<WorkflowDefinitionDto>();

    for (WorkflowDefinition workflowDefinition : deploymentEvent.getDeployedWorkflows()) {
      WorkflowDefinitionDto dto = new WorkflowDefinitionDto();

      dto.setVersion(workflowDefinition.getVersion());
      dto.setKey(workflowDefinition.getBpmnProcessId());
      dto.setResource(new String(deploymentEvent.getBpmnXml(), StandardCharsets.UTF_8));
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
    return "WorkflowDefinitionDto [key=" + key + ", broker=" + broker + ", version=" + version + ", countRunning=" + countRunning + ", countEnded=" + countEnded
        + "]";
  }

  public String getUuid() {
    return uuid;
  }

}
