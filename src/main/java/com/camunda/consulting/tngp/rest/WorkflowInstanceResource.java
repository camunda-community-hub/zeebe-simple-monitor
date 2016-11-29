package com.camunda.consulting.tngp.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.camunda.tngp.client.TngpClient;

import com.camunda.consulting.tngp.dto.BrokerConnectionDto;
import com.camunda.consulting.tngp.dto.WorkflowDefinitionDto;
import com.camunda.consulting.tngp.dto.WorkflowInstanceDto;

@Path("workflow-instance")
@Produces("application/json")
public class WorkflowInstanceResource {
  
  public static List<WorkflowInstanceDto> instances = new ArrayList<>();
  
  @GET
  public List<WorkflowInstanceDto> getWorkflowInstances() {
    return instances;
  }
  
  public static void removeBrokerData(TngpClient client) {
    BrokerConnectionDto brokerConnection = BrokerResource.getBrokerConnection(client); 
    ArrayList<WorkflowInstanceDto> copy = new ArrayList<WorkflowInstanceDto>(instances);
    instances = new ArrayList<WorkflowInstanceDto>();
    for (WorkflowInstanceDto dto : copy) {
      if (!dto.getBroker().equals(brokerConnection.getConnectionString())) {
        instances.add(dto);
      }
    }
  }

  public static void add(TngpClient client, WorkflowInstanceDto instance) {
    WorkflowInstanceDto existingInstance = findInstance(instance.getId());
    if (existingInstance!=null) {
      // TODO: Check
      // Activities might have been added earlier on - as e.g. a start event must be finished before a worklfow instance get started
      instance.getRunningActivities().addAll(existingInstance.getRunningActivities());
      instance.getEndedActivities().addAll(existingInstance.getEndedActivities());
      instances.remove(existingInstance);
    }
    fillInBroker(client, instance);
    instances.add(instance);
    
    adjustCounters();
  }

  public static void setEnded(TngpClient client, long workflowInstanceId) {
    WorkflowInstanceDto workflowInstance = findInstance(workflowInstanceId);
    workflowInstance.setEnded(true);
    adjustCounters();
  }
  
  private static void adjustCounters() {
    HashMap<Long, Long[]> countForWfDefinitionId = new HashMap<Long, Long[]>();
    for (WorkflowInstanceDto workflowInstanceDto : instances) {
      long id = workflowInstanceDto.getWorkflowDefinitionId();
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
    for (Entry<Long, Long[]> count : countForWfDefinitionId.entrySet()) {
      WorkflowDefinitionResource.setCount(count.getKey(), count.getValue()[0], count.getValue()[1]);
    }
  }

  private static WorkflowInstanceDto getOrCreateWorkflowInstanceDto(TngpClient client, long wfInstanceId) {
    WorkflowInstanceDto instance = findInstance(wfInstanceId);
    if (instance==null) {
      instance = new WorkflowInstanceDto();
      instance.setId(wfInstanceId);
      fillInBroker(client, instance);
      instances.add(instance);
    }
    return instance;
  }
  
  public static void addActivityStarted(TngpClient client, long wfInstanceId, String flowElementIdString, String payload) {
    WorkflowInstanceDto instance = getOrCreateWorkflowInstanceDto(client, wfInstanceId);
    
    instance.getRunningActivities().add(flowElementIdString);
    instance.setPayload(payload);  
  }
  public static void addActivityEnded(TngpClient client, long wfInstanceId, String flowElementIdString, String payload) {
    WorkflowInstanceDto instance = getOrCreateWorkflowInstanceDto(client, wfInstanceId);
    
    instance.getRunningActivities().remove(flowElementIdString); // TODO: This does not work with MI like constructs
    instance.getEndedActivities().add(flowElementIdString);
    instance.setPayload(payload);  
  }

  
  private static void fillInBroker(TngpClient client, WorkflowInstanceDto instance) {
    BrokerConnectionDto brokerConnection = BrokerResource.getBrokerConnection(client);
    if (brokerConnection!=null) {
      instance.setBroker(brokerConnection.getConnectionString());
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
