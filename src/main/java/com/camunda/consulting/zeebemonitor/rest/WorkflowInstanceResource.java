package com.camunda.consulting.zeebemonitor.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.camunda.consulting.zeebemonitor.entity.WorkflowInstance;
import com.camunda.consulting.zeebemonitor.repository.WorkflowInstanceRepository;

@RestController
@RequestMapping("/api/workflow-instance")
public class WorkflowInstanceResource {
  
  @Autowired
  private WorkflowInstanceRepository workflowInstanceRepository;
  
  @RequestMapping("/")
  public Iterable<WorkflowInstance> getWorkflowInstances() {
    return workflowInstanceRepository.findAll();
  }
  
}
