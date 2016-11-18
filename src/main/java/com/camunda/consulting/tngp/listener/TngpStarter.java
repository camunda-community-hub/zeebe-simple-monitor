package com.camunda.consulting.tngp.listener;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.camunda.tngp.client.ClientProperties;
import org.camunda.tngp.client.TngpClient;

@Startup
@Singleton
public class TngpStarter {

  // private TngpClient client;

  private TngpEventPolling eventPolling;

  @PostConstruct
  public void init() {
    eventPolling = new TngpEventPolling();
    eventPolling.start();
  }

  @PreDestroy
  public void close() {
    eventPolling.stop();
  }

  public void connectTngpClient(String connectionString) {
    Properties clientProperties = new Properties();
    clientProperties.put(ClientProperties.BROKER_CONTACTPOINT, connectionString);

    TngpClient client = TngpClient.create(clientProperties);

    eventPolling.connectTngpClient(client);
  }

  public void disconnectTngpClient(String connectionString) {
    // TODO
//    eventPolling.disconnectTngpClient(client);
  }
}
