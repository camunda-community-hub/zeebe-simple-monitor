package com.camunda.tngp.monitor;
import java.util.Properties;

import org.camunda.tngp.client.TngpClient;

import com.camunda.consulting.tngp.listener.TngpEventPolling;
import com.camunda.consulting.tngp.rest.WorkflowDefinitionResource;
import com.camunda.consulting.tngp.rest.WorkflowInstanceResource;

public class TngMonitorSysout {

  public static void main(String[] args) throws InterruptedException {
    TngpClient client = TngpClient.create(new Properties());
    client.connect();

    TngpEventPolling eventPolling = new TngpEventPolling();

    eventPolling.pollAllTopics(client);
    
    eventPolling.getEvents(client).forEach( (k,v) -> {  
        System.out.println("TopicId " + k + " ------------"); 
        v.forEach(System.out::println);
    });
    
    
    System.out.println("################### Definitions:");
    System.out.println(WorkflowDefinitionResource.definitions);
    System.out.println("################### Instances:");
    System.out.println(WorkflowInstanceResource.instances);

    client.disconnect();
    client.close();
    // System.out.println(WorkflowDefinitionResource.definitions);
  }
}
