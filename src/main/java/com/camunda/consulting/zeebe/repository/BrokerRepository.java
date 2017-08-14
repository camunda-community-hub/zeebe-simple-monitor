package com.camunda.consulting.zeebe.repository;

import org.springframework.data.repository.CrudRepository;

import com.camunda.consulting.zeebe.entity.Broker;

public interface BrokerRepository extends CrudRepository<Broker, String> {

//  List<BrokerConnection> findByLastName(String lastName);
  
}
