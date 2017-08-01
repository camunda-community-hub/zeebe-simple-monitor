package com.camunda.consulting.zeebe.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.camunda.consulting.zeebe.dto.BrokerConnectionDto;
import com.camunda.consulting.zeebe.dto.WorkflowDefinitionDto;
import com.camunda.consulting.zeebe.dto.WorkflowInstanceDto;

import io.zeebe.client.ZeebeClient;

@RestController
@RequestMapping("/api/workflow-instance")
public class WorkflowInstanceResource {
  
  public static List<WorkflowInstanceDto> instances = new ArrayList<>();
  
  @RequestMapping("/")
  public List<WorkflowInstanceDto> getWorkflowInstances() {
    return instances;
  }
  
  public static void removeBrokerData(ZeebeClient client) {
    BrokerConnectionDto brokerConnection = BrokerResource.getBrokerConnection(client); 
    ArrayList<WorkflowInstanceDto> copy = new ArrayList<WorkflowInstanceDto>(instances);
    instances = new ArrayList<WorkflowInstanceDto>();
    for (WorkflowInstanceDto dto : copy) {
      if (!dto.getBroker().equals(brokerConnection.getConnectionString())) {
        instances.add(dto);
      }
    }
  }

  public static void newWorkflowInstanceStarted(ZeebeClient client, WorkflowInstanceDto instance) {
    WorkflowInstanceDto existingInstance = findInstance(instance.getId());
    if (existingInstance!=null) {
      // TODO: Check
      // Activities might have been added earlier on - as e.g. a start event must be finished before a worklfow instance get started
      instance.getRunningActivities().addAll(existingInstance.getRunningActivities());
      instance.getEndedActivities().addAll(existingInstance.getEndedActivities());
      instances.remove(existingInstance);
    }
    fillInBroker(client, instance);
    fillInWorkflowDefinition(client, instance);
    instances.add(instance);
    
    adjustCounters();
  }


  public static void setEnded(ZeebeClient client, long workflowInstanceId) {
    WorkflowInstanceDto workflowInstance = findInstance(workflowInstanceId);
    workflowInstance.setEnded(true);
    adjustCounters();
  }
  
  private static void adjustCounters() {
    HashMap<String, Long[]> countForWfDefinitionId = new HashMap<String, Long[]>();
    for (WorkflowInstanceDto workflowInstanceDto : instances) {
      String id = workflowInstanceDto.getWorkflowDefinitionUuid();
      boolean ended = workflowInstanceDto.isEnded();
      
      if (!countForWfDefinitionId.containsKey(id)) {
        countForWfDefinitionId.put(id, new Long[]{0l, 0l});
      }
      
      Long[] counts = countForWfDefinitionId.get(id);
      if (ended) {
        counts[1]++;
      } else {
        counts[0]++;
      }
      countForWfDefinitionId.put(id, counts);      
    }
    for (Entry<String, Long[]> count : countForWfDefinitionId.entrySet()) {
      WorkflowDefinitionResource.setCount(count.getKey(), count.getValue()[0], count.getValue()[1]);
    }
  }

  private static WorkflowInstanceDto getOrCreateWorkflowInstanceDto(ZeebeClient client, long wfInstanceId) {
    WorkflowInstanceDto instance = findInstance(wfInstanceId);
    if (instance==null) {
      instance = new WorkflowInstanceDto();
      instance.setId(wfInstanceId);
      fillInBroker(client, instance);
      instances.add(instance);
    }
    return instance;
  }
  
  public static void addActivityStarted(ZeebeClient client, long wfInstanceId, String flowElementIdString, String payload) {
    WorkflowInstanceDto instance = getOrCreateWorkflowInstanceDto(client, wfInstanceId);
    
    instance.getRunningActivities().add(flowElementIdString);
    if (payload!=null && !"".equals(payload)) { // Payload is empty for a lot of elements (e.g. Sequence Flow) at the moment
       instance.setPayload(payload);
    }
  }
  public static void addActivityEnded(ZeebeClient client, long wfInstanceId, String flowElementIdString, String payload) {
    WorkflowInstanceDto instance = getOrCreateWorkflowInstanceDto(client, wfInstanceId);
    
    instance.getRunningActivities().remove(flowElementIdString); // TODO: This does not work with MI like constructs
    instance.getEndedActivities().add(flowElementIdString);
    if (payload!=null && !"".equals(payload)) {
      instance.setPayload(payload);
    }
  }

  
  private static void fillInBroker(ZeebeClient client, WorkflowInstanceDto instance) {
    BrokerConnectionDto brokerConnection = BrokerResource.getBrokerConnection(client);
    if (brokerConnection!=null) {
      instance.setBroker(brokerConnection.getConnectionString());
    }
  }
  
  private static void fillInWorkflowDefinition(ZeebeClient client, WorkflowInstanceDto instance) {
    WorkflowDefinitionDto workflowDefinitionDto = WorkflowDefinitionResource.findInstance(instance.getWorkflowDefinitionKey(), instance.getWorkflowDefinitionVersion());
    if (workflowDefinitionDto!=null) {
      instance.setWorkflowDefinitionUuid(workflowDefinitionDto.getUuid());
      instance.setWorkflowDefinitionKey(workflowDefinitionDto.getKey());
      instance.setWorkflowDefinitionVersion(workflowDefinitionDto.getVersion());
    }
    
  }  
  
  public static WorkflowInstanceDto findInstance(long id) {
    for (WorkflowInstanceDto dto : instances) {
      if (dto.getId()==id) {
        return dto;
      }
    } 
    return null;
  }



  
}
