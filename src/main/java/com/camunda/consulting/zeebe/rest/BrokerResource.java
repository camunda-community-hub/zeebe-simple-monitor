package com.camunda.consulting.zeebe.rest;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.camunda.consulting.zeebe.entity.Broker;
import com.camunda.consulting.zeebe.entity.LoggedEvent;
import com.camunda.consulting.zeebe.repository.BrokerRepository;
import com.camunda.consulting.zeebe.repository.LoggedEventRepository;
import com.camunda.consulting.zeebe.zeebe.ZeebeConnectionDto;
import com.camunda.consulting.zeebe.zeebe.ZeebeConnections;

@Component
@RestController
@RequestMapping("/api/broker")
public class BrokerResource {
  
  @Autowired
  private LoggedEventRepository loggedEventRepository;
  
  @Autowired
  private BrokerRepository brokerRepository;
  
  @Autowired
  private ZeebeConnections zeebeConnections;

//  @PostConstruct
//  public void init(){
//      if (brokerRepository.findOne("127.0.0.1:51015")==null) {
//        Broker broker = new Broker("", "127.0.0.1:51015");
//        brokerRepository.save(broker);
//        zeebeConnections.connect(broker);
//      }
//  }

  @RequestMapping("/log")
  public Iterable<LoggedEvent> getLogs() {
    return loggedEventRepository.findAll();
  }
  
  @RequestMapping("/")
  public List<ZeebeConnectionDto> getBrokerConnections() {
    return zeebeConnections.getConnectionDtoList();
  }

  @RequestMapping(path="/connect", method=RequestMethod.POST)
  public ZeebeConnectionDto connect( @RequestBody String connectionString) {
    Broker broker = brokerRepository.findOne(connectionString);
    if (broker==null) {
      broker = new Broker(null, connectionString); // TODO: Add names to UI
      brokerRepository.save(broker);
    }
    
    if (!zeebeConnections.isConnected(broker)) {
      zeebeConnections.connect(broker);
    }
    
    return zeebeConnections.getConnectionDto(broker); 
  }

  @RequestMapping(path="/disconnect", method=RequestMethod.POST)
  public ZeebeConnectionDto disconnect(@RequestBody String connectionString) {
    Broker broker = brokerRepository.findOne(connectionString);
    if (broker!=null) {
      if (zeebeConnections.isConnected(broker)) {
        zeebeConnections.disconnect(broker);
      }
      return zeebeConnections.getConnectionDto(broker); 
    }
    return null;
  }

  
  @RequestMapping(path="/cleanup", method=RequestMethod.POST)
  public void cleanup() {
    // TODO: Cleanup for only one broker?
      zeebeConnections.deleteAllData();
  }

}
