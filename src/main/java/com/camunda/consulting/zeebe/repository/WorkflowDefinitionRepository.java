package com.camunda.consulting.zeebe.repository;

import org.springframework.data.repository.CrudRepository;

import com.camunda.consulting.zeebe.entity.WorkflowDefinition;

public interface WorkflowDefinitionRepository extends CrudRepository<WorkflowDefinition, String> {

  WorkflowDefinition findByBrokerConnectionStringAndKeyAndVersion(String brokerConnectionString, String key, int version);
  
}
