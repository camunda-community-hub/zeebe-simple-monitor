package com.camunda.consulting.zeebemonitor.repository;

import org.springframework.data.repository.CrudRepository;

import com.camunda.consulting.zeebemonitor.entity.Broker;

public interface BrokerRepository extends CrudRepository<Broker, String> {

//  List<BrokerConnection> findByLastName(String lastName);
  
}
