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

@Path("workflow-definition")
@Produces("application/json")
public class WorkflowDefinitionResource {
  
  public static List<WorkflowDefinitionDto> definitions = new ArrayList<>();
  
  @GET
  public List<WorkflowDefinitionDto> getWorkflowDefinitions() {
    return definitions;
  }

  @GET
  @Path("{id}")
  public WorkflowDefinitionDto findWorkflowDefinition(@PathParam("id") long id) {
    return findInstance(id);
  }

  
  public static void removeBrokerData(TngpClient client) {
    BrokerConnectionDto brokerConnection = BrokerResource.getBrokerConnection(client); 
    ArrayList<WorkflowDefinitionDto> copy = new ArrayList<WorkflowDefinitionDto>(definitions);
    definitions = new ArrayList<>();
    for (WorkflowDefinitionDto workflowDefinitionDto : copy) {
      if (!workflowDefinitionDto.getBroker().equals(brokerConnection.getConnectionString())) {
        definitions.add(workflowDefinitionDto);
      }
    }
  }

  public static void add(TngpClient client, WorkflowDefinitionDto definition) {
    BrokerConnectionDto brokerConnection = BrokerResource.getBrokerConnection(client);
    if (brokerConnection!=null) {
      definition.setBroker(brokerConnection.getConnectionString());
    }
    definitions.add(definition);    
  }

  public static WorkflowDefinitionDto findInstance(long id) {
    for (WorkflowDefinitionDto dto : definitions) {
      if (dto.getId()==id) {
        return dto;
      }
    } 
    return null;
  }
}
