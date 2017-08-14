package com.camunda.consulting.zeebe.rest;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.camunda.consulting.zeebe.Constants;
import com.camunda.consulting.zeebe.entity.DeploymentDto;
import com.camunda.consulting.zeebe.entity.FileDto;
import com.camunda.consulting.zeebe.entity.WorkflowDefinition;
import com.camunda.consulting.zeebe.repository.WorkflowDefinitionRepository;
import com.camunda.consulting.zeebe.repository.WorkflowInstanceRepository;
import com.camunda.consulting.zeebe.zeebe.ZeebeConnections;

import io.zeebe.client.WorkflowsClient;

@RestController
@RequestMapping(path="/api/workflow-definition")
public class WorkflowDefinitionResource {

  @Autowired
  private ZeebeConnections connections;

  @Autowired
  private WorkflowDefinitionRepository workflowDefinitionRepository;

  @Autowired
  private WorkflowInstanceRepository workflowInstanceRepository;

  @RequestMapping(path="/")
  public Iterable<WorkflowDefinition> getWorkflowDefinitions() {
    return fillWorkflowInstanceCount(workflowDefinitionRepository.findAll());
  }

  private Iterable<WorkflowDefinition> fillWorkflowInstanceCount(Iterable<WorkflowDefinition> workflowDefinitions) {
    for (WorkflowDefinition workflowDefinition : workflowDefinitions) {
      fillWorkflowInstanceCount(workflowDefinition);
    }
    return workflowDefinitions;
  }

  private WorkflowDefinition fillWorkflowInstanceCount(WorkflowDefinition workflowDefinition) {
    workflowDefinition.setCountRunning(workflowInstanceRepository.countRunningInstances(workflowDefinition.getKey(), workflowDefinition.getVersion()));
    workflowDefinition.setCountEnded(workflowInstanceRepository.countEndedInstances(workflowDefinition.getKey(), workflowDefinition.getVersion()));
    return workflowDefinition;
  }

  @RequestMapping(path="/{broker}/{key}/{version}")
  public WorkflowDefinition findWorkflowDefinition(@PathVariable("broker") String broker, @PathVariable("key") String key, @PathVariable("version") int version) {
    return fillWorkflowInstanceCount(workflowDefinitionRepository.findByBrokerConnectionStringAndKeyAndVersion(broker, key, version));
  }

  @RequestMapping(path="/{broker}/{id}", method=RequestMethod.PUT)
  public void startWorkflowInstance(@PathVariable("id") String id, @PathVariable("broker") String brokerConnection, String payload) {
    connections.getZeebeClient(brokerConnection).workflows() //
      .create(Constants.DEFAULT_TOPIC)
      .bpmnProcessId(id)
      .payload(payload)
      .execute();
  }
  
  @RequestMapping(path="/", method=RequestMethod.POST)
  public void uploadModel(@RequestBody DeploymentDto deployment) throws UnsupportedEncodingException {
    WorkflowsClient workflows = connections.getZeebeClient(deployment.getBroker()).workflows();    
    for (FileDto file : deployment.getFiles()) {
      workflows //
          .deploy(Constants.DEFAULT_TOPIC) //
          .resourceStream(new ByteArrayInputStream(file.getContent()))
          .execute();
    }
  }
 
}
