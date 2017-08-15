package com.camunda.consulting.zeebemonitor.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.camunda.consulting.zeebemonitor.entity.WorkflowInstance;

import org.springframework.data.jpa.repository.Query;


public interface WorkflowInstanceRepository extends CrudRepository<WorkflowInstance, Long> {

  List<WorkflowInstance> findByBrokerConnectionString(String brokerId);   
  
  @Query("SELECT COUNT(wf) FROM WorkflowInstance wf WHERE wf.workflowDefinitionKey=?1 and wf.workflowDefinitionVersion=?2 and wf.ended=false")
  long countRunningInstances(String workflowDefinitionKey, int workflowDefinitionVersion);

  @Query("SELECT COUNT(wf) FROM WorkflowInstance wf WHERE wf.workflowDefinitionKey=?1 and wf.workflowDefinitionVersion=?2 and wf.ended=true")
  long countEndedInstances(String workflowDefinitionKey, int workflowDefinitionVersion);

}
