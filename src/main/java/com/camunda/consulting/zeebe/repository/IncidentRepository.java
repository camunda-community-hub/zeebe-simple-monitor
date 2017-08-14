package com.camunda.consulting.zeebe.repository;

import org.springframework.data.repository.CrudRepository;

import com.camunda.consulting.zeebe.entity.Incident;

public interface IncidentRepository extends CrudRepository<Incident, Long> {

//  List<BrokerConnection> findByLastName(String lastName);
  
}
