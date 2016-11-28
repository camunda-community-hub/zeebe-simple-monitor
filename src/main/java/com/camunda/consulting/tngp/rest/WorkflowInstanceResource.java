package com.camunda.consulting.tngp.rest;

import java.util.ArrayList;
import java.util.List;

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
    BrokerConnectionDto brokerConnection = BrokerResource.getBrokerConnection(client);
    if (brokerConnection!=null) {
      instance.setBroker(brokerConnection.getConnectionString());
    }
    instances.add(instance);    
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
