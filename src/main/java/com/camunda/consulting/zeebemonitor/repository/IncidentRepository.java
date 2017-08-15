package com.camunda.consulting.zeebemonitor.repository;

import org.springframework.data.repository.CrudRepository;

import com.camunda.consulting.zeebemonitor.entity.Incident;

public interface IncidentRepository extends CrudRepository<Incident, Long> {

//  List<BrokerConnection> findByLastName(String lastName);
  
}
