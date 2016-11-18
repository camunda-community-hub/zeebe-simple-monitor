package com.camunda.consulting.tngp.rest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.camunda.tngp.broker.Broker;

import com.camunda.consulting.tngp.dto.WorkflowDefinitionDto;
import com.camunda.consulting.tngp.listener.TngpStarter;

@Path("broker")
@Produces("application/json")
@Singleton
public class BrokerResource {
  
  @Inject
  private TngpStarter starter;
  
  public static List<WorkflowDefinitionDto> definitions = new ArrayList<>();

  private Broker broker;
  
  @POST
  @Path("connect")
  public void connect(String connectionString) {
    starter.connectTngpClient(connectionString);
  }
  @POST
  @Path("disconnect")
  public void disconnect(String connectionString) {
    starter.disconnectTngpClient(connectionString);
  }

  @POST
  @Path("embedded/start")
  public void startEmbeddedBroker() {
    InputStream config = BrokerResource.class.getResourceAsStream("/tngp.cfg.toml");
    broker = new Broker(config);
  }

  @POST
  @Path("embedded/stop")
  public void stopEmbeddedBroker() {
    broker.close();
    broker = null;
  }
  
  @PreDestroy
  public void cleanup() {
    if (broker!=null) {
      broker.close();
      broker = null;
    }
  }
}
