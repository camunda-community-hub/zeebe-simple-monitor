package com.camunda.consulting.tngp.rest;

import java.io.InputStream;
import java.text.spi.BreakIteratorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.camunda.tngp.broker.Broker;
import org.camunda.tngp.client.ClientProperties;
import org.camunda.tngp.client.TngpClient;

import com.camunda.consulting.tngp.dto.BrokerConnectionDto;
import com.camunda.consulting.tngp.listener.TngpEventPolling;

@Path("broker")
@Produces("application/json")
@Singleton
public class BrokerResource {

  private TngpEventPolling eventPolling;
  private static List<BrokerConnectionDto> brokerConnections = new ArrayList<BrokerConnectionDto>();

  private Broker embeddedBroker;

  public static BrokerConnectionDto getBrokerConnection(TngpClient client) {
    for (BrokerConnectionDto current : brokerConnections) {
      if (current.getClient() == client) {
        return current;
      }
    }
    return null;
  }

  public static BrokerConnectionDto getBrokerConnection(String connectionString) {
    for (BrokerConnectionDto current : brokerConnections) {
      if (current.getConnectionString().equals(connectionString)) {
        return current;
      }
    }
    return null;
  }

  @GET
  public List<BrokerConnectionDto> getBrokerConnections() {
    return brokerConnections;
  }

  @POST
  @Path("connect")
  public BrokerConnectionDto connect(String connectionString) {
    BrokerConnectionDto newConnection = null;
    for (BrokerConnectionDto brokerConnectionDto : brokerConnections) {
      if (brokerConnectionDto.getConnectionString().equals(connectionString)
          && brokerConnectionDto.isConnected()) {
        return brokerConnectionDto; // no duplicate connection
      }
      if (brokerConnectionDto.getConnectionString().equals(connectionString)
          && !brokerConnectionDto.isConnected()) {
        newConnection = brokerConnectionDto; // no duplicate connection
      }
    }
    if (newConnection==null) {
      newConnection = new BrokerConnectionDto("", connectionString, true, null);
      brokerConnections.add(newConnection);      
    }

    Properties clientProperties = new Properties();
    clientProperties.put(ClientProperties.BROKER_CONTACTPOINT, connectionString);

    TngpClient client = TngpClient.create(clientProperties);
    newConnection.setClient(client);
    newConnection.setConnected(true);
    
    eventPolling.connectTngpClient(client);
    
    System.out.println("Connected new client " + newConnection);
    
    return newConnection;
  }

  @POST
  @Path("disconnect")
  public BrokerConnectionDto disconnect(String connectionString) {
    for (BrokerConnectionDto brokerConnectionDto : brokerConnections) {
      if (brokerConnectionDto.getConnectionString().equals(connectionString)) {
        eventPolling.disconnectTngpClient(brokerConnectionDto.getClient());
        brokerConnectionDto.setConnected(false);
        return brokerConnectionDto;
      }
    }
    return null;
  }

  @PostConstruct
  public void init() {
    eventPolling = new TngpEventPolling();
    eventPolling.start();
    
    brokerConnections.add(new BrokerConnectionDto("", "127.0.0.1:51015", false, null));
  }

  @POST
  @Path("embedded/start")
  public void startEmbeddedBroker() {
    if (embeddedBroker!=null) {
      return; 
    }
    InputStream config = BrokerResource.class.getResourceAsStream("/tngp.cfg.toml");
    embeddedBroker = new Broker(config);
  }

  @POST
  @Path("embedded/stop")
  public void stopEmbeddedBroker() {
    if (embeddedBroker==null) {
      return; 
    }
    embeddedBroker.close();
    embeddedBroker = null;
  }

  @PreDestroy
  public void cleanup() {
    eventPolling.stop();

    if (embeddedBroker != null) {
      embeddedBroker.close();
      embeddedBroker = null;
    }
  }
}
