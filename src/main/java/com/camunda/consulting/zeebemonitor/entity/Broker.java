package com.camunda.consulting.zeebemonitor.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Broker {

//  @GeneratedValue
//  private String id; // = UUID.randomUUID().toString()

  @Id
  private String connectionString;
  private String name;
  
  public Broker() {    
  }
  
  public Broker(String name, String connectionString) {
    super();
    this.name = name;
    this.connectionString = connectionString;  
  }
  public String getConnectionString() {
    return connectionString;
  }
  public String getName() {
    return name;
  }
  
}
