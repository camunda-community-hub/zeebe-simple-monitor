package com.camunda.consulting.zeebe.rest;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.camunda.consulting.zeebe.Constants;
import com.camunda.consulting.zeebe.dto.BrokerConnectionDto;
import com.camunda.consulting.zeebe.dto.DeploymentDto;
import com.camunda.consulting.zeebe.dto.FileDto;
import com.camunda.consulting.zeebe.dto.WorkflowDefinitionDto;

import io.zeebe.client.ZeebeClient;

@RestController
@RequestMapping(path="/api/workflow-definition")
public class WorkflowDefinitionResource {

  
  public static List<WorkflowDefinitionDto> definitions = new ArrayList<>();

  @RequestMapping(path="/")
  public List<WorkflowDefinitionDto> getWorkflowDefinitions() {
    return definitions;
  }

  @RequestMapping(path="/{key}/{version}")
  public WorkflowDefinitionDto findWorkflowDefinition(@PathVariable("key") String key, @PathVariable("version") int version) {
    return findInstance(key, version);
  }

  @RequestMapping(path="/{broker}/{id}", method=RequestMethod.PUT)
  public void startWorkflowInstance(@PathVariable("id") String id, @PathVariable("broker") String brokerConnection, String payload) {
    ZeebeClient client = BrokerResource.getBrokerConnection(brokerConnection).getClient();
    client.workflows().create(Constants.DEFAULT_TOPIC)
      .bpmnProcessId(id)
      .payload(payload)
      .execute();
  }
  
  @RequestMapping(path="/", method=RequestMethod.POST)
  public void uploadModel(@RequestBody DeploymentDto deployment) throws UnsupportedEncodingException {
    ZeebeClient client = BrokerResource.getBrokerConnection(deployment.getBroker()).getClient();    

    for (FileDto file : deployment.getFiles()) {
      client.workflows() //
          .deploy(Constants.DEFAULT_TOPIC) //
          .resourceStream(new ByteArrayInputStream(file.getContent()))
          .execute();
    }
  }

  public static void removeBrokerData(ZeebeClient client) {
    BrokerConnectionDto brokerConnection = BrokerResource.getBrokerConnection(client);
    ArrayList<WorkflowDefinitionDto> copy = new ArrayList<WorkflowDefinitionDto>(definitions);
    definitions = new ArrayList<>();
    for (WorkflowDefinitionDto workflowDefinitionDto : copy) {
      if (!workflowDefinitionDto.getBroker().equals(brokerConnection.getConnectionString())) {
        definitions.add(workflowDefinitionDto);
      }
    }
  }

  public static void add(ZeebeClient client, WorkflowDefinitionDto newDefinition) {
    BrokerConnectionDto brokerConnection = BrokerResource.getBrokerConnection(client);
    newDefinition.setBroker(brokerConnection.getConnectionString());
    definitions.add(newDefinition);
  }
  
  public static void addAll(ZeebeClient client, List<WorkflowDefinitionDto> newDefinitions) {
    BrokerConnectionDto brokerConnection = BrokerResource.getBrokerConnection(client);
    if (brokerConnection != null) {
      for (WorkflowDefinitionDto definition : newDefinitions) {        
        definition.setBroker(brokerConnection.getConnectionString());
      }
    }
    definitions.addAll(newDefinitions);
  }

  public static void setCount(String workflowDefinitionUuid, long countRunning, long countEnded) {
    WorkflowDefinitionDto workflowDefinitionDto = findInstance(workflowDefinitionUuid);
    workflowDefinitionDto.setCountRunning(countRunning);
    workflowDefinitionDto.setCountEnded(countEnded);
  }
 
  public static WorkflowDefinitionDto findInstance(String key, int version) {
    for (WorkflowDefinitionDto dto : definitions) {
      if (dto.getKey().equals(key) && dto.getVersion() == version) {
        return dto;
      }
    }
    return null;
  }

  public static WorkflowDefinitionDto findInstance(String uuid) {
    for (WorkflowDefinitionDto dto : definitions) {
      if (dto.getUuid().equals(uuid)) {
        return dto;
      }
    }
    return null;
  }
 
}
