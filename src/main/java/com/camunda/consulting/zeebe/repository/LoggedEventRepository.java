package com.camunda.consulting.zeebe.repository;

import org.springframework.data.repository.CrudRepository;

import com.camunda.consulting.zeebe.entity.LoggedEvent;

public interface LoggedEventRepository extends CrudRepository<LoggedEvent, Long> {

//  List<BrokerConnection> findByLastName(String lastName);
  
}
