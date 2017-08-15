package com.camunda.consulting.zeebemonitor.repository;

import org.springframework.data.repository.CrudRepository;

import com.camunda.consulting.zeebemonitor.entity.LoggedEvent;

public interface LoggedEventRepository extends CrudRepository<LoggedEvent, Long> {

//  List<BrokerConnection> findByLastName(String lastName);
  
}
