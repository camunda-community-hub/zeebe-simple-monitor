package com.camunda.consulting.zeebe.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.camunda.consulting.zeebe.dto.BrokerConnectionDto;
import com.camunda.consulting.zeebe.listener.ZeebeListener;

import io.zeebe.client.ClientProperties;
import io.zeebe.client.ZeebeClient;

@RestController
@RequestMapping("/api/broker")
public class BrokerResource {

  private static ZeebeListener zeebeListener = new ZeebeListener();
  private static List<BrokerConnectionDto> brokerConnections = new ArrayList<BrokerConnectionDto>();

//  private static Broker embeddedBroker;
  
  static {
    brokerConnections.add(new BrokerConnectionDto("", "127.0.0.1:51015", false, null));
  }

  public static BrokerConnectionDto getBrokerConnection(ZeebeClient client) {
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

  @RequestMapping("/log")
  public Map<String, Map<Integer, List<String>>> getLogs() {
    return ZeebeListener.events;
  }
  
  @RequestMapping("/")
  public List<BrokerConnectionDto> getBrokerConnections() {
    return brokerConnections;
  }

  @RequestMapping(path="/connect", method=RequestMethod.POST)
  public BrokerConnectionDto connect( @RequestBody String connectionString) {
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

    ZeebeClient client = ZeebeClient.create(clientProperties);
    client.connect();
    
    newConnection.setClient(client);
    newConnection.setConnected(true);

    try {
      zeebeListener.connectTngpClient(client);
      System.out.println("Connected new client " + newConnection);
    } catch (Exception ex) {
      newConnection.setConnected(false);
      System.out.println("Could not connect to broker " + newConnection);
      ex.printStackTrace();
    }
    
    
    return newConnection; 
  }

  @RequestMapping(path="/disconnect", method=RequestMethod.POST)
  public BrokerConnectionDto disconnect(String connectionString) {
    for (BrokerConnectionDto brokerConnectionDto : brokerConnections) {
      if (brokerConnectionDto.getConnectionString().equals(connectionString)) {
        zeebeListener.disconnectTngpClient(brokerConnectionDto.getClient());
        brokerConnectionDto.setConnected(false);
        return brokerConnectionDto;
      }
    }
    return null;
  }


  
//  @RequestMapping(path="/embedded/start", method=RequestMethod.POST)  
//  public void startEmbeddedBroker() {
//    if (embeddedBroker!=null) {
//      return; 
//    }
//    InputStream config = BrokerResource.class.getResourceAsStream("/zeebe.cfg.toml");
//    embeddedBroker = new Broker(config);
//  }
//
//  @RequestMapping(path="/embedded/stop", method=RequestMethod.POST)  public void stopEmbeddedBroker() {
//    if (embeddedBroker==null) {
//      return; 
//    }
//    embeddedBroker.close();
//    embeddedBroker = null;
//  }

 
}
