package com.camunda.consulting.tngp.rest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.tngp.client.TngpClient;

import com.camunda.consulting.tngp.dto.BrokerConnectionDto;
import com.camunda.consulting.tngp.dto.DeploymentDto;
import com.camunda.consulting.tngp.dto.FileDto;
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

  @PUT
  @Path("{broker}/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public void startWorkflowInstance(@PathParam("id") long id, @PathParam("broker") String brokerConnection, String payload) {
    TngpClient client = BrokerResource.getBrokerConnection(brokerConnection).getClient();
    client.workflows().start()
      .workflowDefinitionId(id)
      .payload(payload)
      .execute();
  }
  
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public void uploadModel(DeploymentDto deployment) throws UnsupportedEncodingException {

    TngpClient client = BrokerResource.getBrokerConnection(deployment.getBroker()).getClient();
    ;

    for (FileDto file : deployment.getFiles()) {
      client.workflows() //
          .deploy() //
          .resourceBytes(file.getContent()) //
          .execute();
    }
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
    if (brokerConnection != null) {
      definition.setBroker(brokerConnection.getConnectionString());
    }
    definitions.add(definition);
  }

  public static void setCount(long workflowDefinitionId, long countRunning, long countEnded) {
    WorkflowDefinitionDto workflowDefinitionDto = findInstance(workflowDefinitionId);
    workflowDefinitionDto.setCountRunning(countRunning);
    workflowDefinitionDto.setCountEnded(countEnded);
  }
 
  public static WorkflowDefinitionDto findInstance(long id) {
    for (WorkflowDefinitionDto dto : definitions) {
      if (dto.getId() == id) {
        return dto;
      }
    }
    return null;
  }

 
}
