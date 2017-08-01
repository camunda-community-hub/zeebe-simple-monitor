package com.camunda.consulting.zeebe.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.zeebe.client.ZeebeClient;

public class BrokerConnectionDto {

  private String connectionString;
  private String name;
  private boolean connected = false;
  private ZeebeClient client;
   
  public BrokerConnectionDto(String name, String connectionString, boolean connected, ZeebeClient client) {
    super();
    this.name = name;
    this.connectionString = connectionString;
    this.connected = connected;
    this.client = client;
  }
  public String getConnectionString() {
    return connectionString;
  }
  public String getName() {
    return name;
  }
  public boolean isConnected() {
    return connected;
  }
  
  @JsonIgnore
  public ZeebeClient getClient() {
    return client;
  }
  public void setConnected(boolean connected) {
    this.connected = connected;
  }
  public void setClient(ZeebeClient client) {
    this.client = client;
  }
  @Override
  public String toString() {
    return "BrokerConnectionDto [connectionString=" + connectionString + ", name=" + name + ", connected=" + connected + "]";
  }
}
