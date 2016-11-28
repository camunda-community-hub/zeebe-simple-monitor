package com.camunda.consulting.tngp.dto;

import org.camunda.tngp.client.TngpClient;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class BrokerConnectionDto {

  private String connectionString;
  private String name;
  private boolean connected = false;
  private TngpClient client;
   
  public BrokerConnectionDto(String name, String connectionString, boolean connected, TngpClient client) {
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
  public TngpClient getClient() {
    return client;
  }
  public void setConnected(boolean connected) {
    this.connected = connected;
  }
  public void setClient(TngpClient client) {
    this.client = client;
  }
  @Override
  public String toString() {
    return "BrokerConnectionDto [connectionString=" + connectionString + ", name=" + name + ", connected=" + connected + "]";
  }
}
