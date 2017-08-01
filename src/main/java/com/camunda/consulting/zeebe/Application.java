package com.camunda.consulting.zeebe;

import javax.annotation.PreDestroy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.camunda.consulting.zeebe.dto.BrokerConnectionDto;
import com.camunda.consulting.zeebe.listener.ZeebeListener;
import com.camunda.consulting.zeebe.rest.BrokerResource;

@SpringBootApplication
@EnableAutoConfiguration
public class Application {

  public static void main(String... args) {
    SpringApplication.run(Application.class, args);
    
    init();
  }
  
  public static void init() {
//    eventPolling = new ZeebeListener();
//    eventPolling.start();
//   
  }
  
//  @PreDestroy
//  public void cleanup() {
//    zeebeListener.stop();
//
//    if (embeddedBroker != null) {
//      embeddedBroker.close();
//      embeddedBroker = null;
//    }
//  }

}
