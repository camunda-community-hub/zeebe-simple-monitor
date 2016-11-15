package com.camunda.consulting.tngp.listener;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.camunda.tngp.client.TngpClient;

@Startup
@Singleton
public class TngpStarter {

  private TngpClient client;
  
  private TngpEventPolling eventPolling;

  @PostConstruct
  public void init() {
    // Properties clientProperties = new Properties();
    // clientProperties.put(ClientProperties.BROKER_CONTACTPOINT,
    // "127.0.0.1:51015");

    client = TngpClient.create(new Properties());
    client.connect();
    
    eventPolling = new TngpEventPolling(client);
    eventPolling.start();
  }
      
  
//  @Produces
//  public TngpClient client() {
//    return client;
//  }
  
  @PreDestroy
  public void close() {
    eventPolling.stop();
    client.disconnect();
    client.close();
  }
}
