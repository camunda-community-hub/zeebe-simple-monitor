package com.camunda.consulting.zeebemonitor.repository;

import org.springframework.data.repository.CrudRepository;

import com.camunda.consulting.zeebemonitor.entity.WorkflowDefinition;

public interface WorkflowDefinitionRepository extends CrudRepository<WorkflowDefinition, String> {

  WorkflowDefinition findByBrokerConnectionStringAndKeyAndVersion(String brokerConnectionString, String key, int version);
  
}
