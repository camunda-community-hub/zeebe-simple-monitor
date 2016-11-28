package com.camunda.consulting.tngp.listener;

import java.util.HashMap;
import java.util.Map;

import org.camunda.tngp.client.TngpClient;
import org.camunda.tngp.client.event.Event;
import org.camunda.tngp.client.event.EventsBatch;
import org.camunda.tngp.client.event.impl.dto.TaskInstanceEventImpl;
import org.camunda.tngp.client.event.impl.dto.UnknownEvent;
import org.camunda.tngp.client.event.impl.dto.WorkflowDefinitionEventImpl;
import org.camunda.tngp.client.event.impl.dto.WorkflowDefinitionRequestEventImpl;
import org.camunda.tngp.protocol.log.ActivityInstanceRequestDecoder;
import org.camunda.tngp.protocol.log.BpmnActivityEventDecoder;
import org.camunda.tngp.protocol.log.BpmnFlowElementEventDecoder;
import org.camunda.tngp.protocol.log.BpmnProcessEventDecoder;
import org.camunda.tngp.protocol.log.CreateTaskRequestDecoder;
import org.camunda.tngp.protocol.log.MessageHeaderDecoder;
import org.camunda.tngp.protocol.log.TaskInstanceDecoder;
import org.camunda.tngp.protocol.log.TaskInstanceRequestDecoder;
import org.camunda.tngp.protocol.log.WorkflowInstanceRequestDecoder;

import com.camunda.consulting.tngp.dto.WorkflowDefinitionDto;
import com.camunda.consulting.tngp.dto.WorkflowInstanceDto;
import com.camunda.consulting.tngp.rest.BrokerResource;
import com.camunda.consulting.tngp.rest.WorkflowDefinitionResource;
import com.camunda.consulting.tngp.rest.WorkflowInstanceResource;

public class TngpEventPolling {

  protected static final long POLLING_DELAY = 500;
  
  private Map<TngpClient, Map<Integer, Long>> tngpClients = new HashMap<TngpClient, Map<Integer, Long>>();
  
  public void connectTngpClient(TngpClient client) {
    client.connect();
    HashMap<Integer, Long> positions = new HashMap<Integer, Long>();
    positions.put(0, 0l);
    positions.put(1, 0l);
    positions.put(2, 0l);
    tngpClients.put(client, positions);
  }

  public void disconnectTngpClient(TngpClient client) {
    tngpClients.remove(client);    
    client.disconnect();
    client.close();
    
    WorkflowDefinitionResource.removeBrokerData(client);
  }

  private boolean isRunning = false;

  public void pollAllTopicsForAllClients() {
    for (TngpClient tngpClient : tngpClients.keySet()) {            
      pollAllTopics(tngpClient);
    }
  }
  
  public void pollAllTopics(TngpClient tngpClient) {
//    System.out.println("########### POLL default-task-queue-log");
    poll(tngpClient, 0); // 0 = default-task-queue-log
//    System.out.println("########### POLL default-wf-definition-log");
    poll(tngpClient, 1); // 1 = default-wf-definition-log
//    System.out.println("########### POLL default-wf-instance-log");
//    poll(tngpClient, 2); // 2 = default-wf-instance-log
  }
  
  public void poll(TngpClient tngpClient, int topicId) {
    long startPosition = 0;
    if (tngpClients!=null && tngpClients.containsKey(tngpClient)) {      
      startPosition = tngpClients.get(tngpClient).get(topicId);
    }
    
    EventsBatch eventsBatch = tngpClient.events().poll() //
        .startPosition(startPosition) //
        .maxEvents(100) // Integer.MAX_VALUE
        .topicId(topicId) //
        .execute();

    
    for (Event evt : eventsBatch.getEvents()) {
      System.out.println(evt.getPosition());
      // Adjust log pointer for next query
      if (evt.getPosition()>=startPosition) {
        startPosition = evt.getPosition()+1;
        if (tngpClients!=null && tngpClients.containsKey(tngpClient)) {      
          tngpClients.get(tngpClient).put(topicId, startPosition);
        }
      }
      
      // Events already parsed in Client Lib
      if (evt instanceof WorkflowDefinitionEventImpl) {
        handle(tngpClient, (WorkflowDefinitionEventImpl)evt);        
      }
      else if (evt instanceof TaskInstanceEventImpl) {
        handle((TaskInstanceEventImpl)evt);        
      }
      else if (evt instanceof WorkflowDefinitionRequestEventImpl) {
        handle((WorkflowDefinitionRequestEventImpl)evt);        
      }
      // Events not yet parsed in client lib, decode ourselves
      else if (evt instanceof UnknownEvent) {
        MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder().wrap(evt.getRawBuffer(), 0);
        
        switch (((UnknownEvent) evt).getTemplateId()) {
        case BpmnProcessEventDecoder.TEMPLATE_ID: {
          BpmnProcessEventDecoder decoder = new BpmnProcessEventDecoder() //
            .wrap(evt.getRawBuffer(), 0 + headerDecoder.encodedLength(), headerDecoder.blockLength(), headerDecoder.version());          
          handle(decoder);
          break;
        }
        case BpmnActivityEventDecoder.TEMPLATE_ID: {
          BpmnActivityEventDecoder decoder = new BpmnActivityEventDecoder() //
            .wrap(evt.getRawBuffer(), 0 + headerDecoder.encodedLength(), headerDecoder.blockLength(), headerDecoder.version());          
          handle(decoder);
          break;
        }
        case BpmnFlowElementEventDecoder.TEMPLATE_ID: {
          BpmnFlowElementEventDecoder decoder = new BpmnFlowElementEventDecoder() //
              .wrap(evt.getRawBuffer(), 0 + headerDecoder.encodedLength(), headerDecoder.blockLength(), headerDecoder.version());          
            handle(tngpClient, decoder);
            break;
        }
        case TaskInstanceDecoder.TEMPLATE_ID: {
          TaskInstanceDecoder decoder = new TaskInstanceDecoder() //
              .wrap(evt.getRawBuffer(), 0 + headerDecoder.encodedLength(), headerDecoder.blockLength(), headerDecoder.version());          
          handle(decoder);
          break;
        }
        case WorkflowInstanceRequestDecoder.TEMPLATE_ID: {
          WorkflowInstanceRequestDecoder decoder = new WorkflowInstanceRequestDecoder() //
              .wrap(evt.getRawBuffer(), 0 + headerDecoder.encodedLength(), headerDecoder.blockLength(), headerDecoder.version());          
          handle(tngpClient, decoder);
          break;
        }
        case ActivityInstanceRequestDecoder.TEMPLATE_ID: {
          ActivityInstanceRequestDecoder decoder = new ActivityInstanceRequestDecoder() //
              .wrap(evt.getRawBuffer(), 0 + headerDecoder.encodedLength(), headerDecoder.blockLength(), headerDecoder.version());          
          handle(decoder);
          break;
        }
        case TaskInstanceRequestDecoder.TEMPLATE_ID: {
          TaskInstanceRequestDecoder decoder = new TaskInstanceRequestDecoder() //
              .wrap(evt.getRawBuffer(), 0 + headerDecoder.encodedLength(), headerDecoder.blockLength(), headerDecoder.version());          
          handle(decoder);
          break;
        }
        case CreateTaskRequestDecoder.TEMPLATE_ID: {
          CreateTaskRequestDecoder decoder = new CreateTaskRequestDecoder() //
              .wrap(evt.getRawBuffer(), 0 + headerDecoder.encodedLength(), headerDecoder.blockLength(), headerDecoder.version());          
          handle(decoder);
          break;
        }
        }

      }
      else {
        System.out.println("Event of type " + evt.getClass() + " unkown: " + evt);
      }
    }
  }



  private void handle(CreateTaskRequestDecoder decoder) {
    System.out.println(decoder);
  }

  private void handle(TaskInstanceRequestDecoder decoder) {
    System.out.println(decoder);
  }

  private void handle(ActivityInstanceRequestDecoder decoder) {
    System.out.println(decoder);
  }

  private void handle(BpmnProcessEventDecoder decoder) {
    System.out.println(decoder);
    System.out.println("DefId: " + decoder.wfDefinitionId());
    System.out.println("InstanceId: " + decoder.wfInstanceId());
    
    System.out.println("key: " + decoder.key()); // identifier for "local state machine" / compare to primary key
  }

  private void handle(TngpClient client, WorkflowInstanceRequestDecoder decoder) {
    System.out.println(decoder);
    WorkflowInstanceResource.add(client, WorkflowInstanceDto.from(decoder));
  }

  private void handle(TaskInstanceDecoder decoder) {
    System.out.println(decoder);
  }

  private void handle(TngpClient client, BpmnFlowElementEventDecoder decoder) {
    System.out.println(decoder);
    
    WorkflowInstanceDto instance = WorkflowInstanceResource.findInstance(decoder.wfInstanceId());
    instance.addActivity(decoder.flowElementIdString(), decoder.payload());
  }

  private void handle(BpmnActivityEventDecoder decoder) {
    System.out.println(decoder);
    System.out.println("Instance: " + decoder.wfInstanceId());
    System.out.println("Definition: " + decoder.wfDefinitionId());
    System.out.println("Flow Element: " +  decoder.flowElementIdString());
  }

  private void handle(TaskInstanceEventImpl evt) {
    System.out.println(evt);
  }

  private void handle(TngpClient client, WorkflowDefinitionEventImpl evt) {
    System.out.println(evt);
    WorkflowDefinitionResource.add(
        client,
        WorkflowDefinitionDto.from(evt));
  }
  
  private void handle(WorkflowDefinitionRequestEventImpl evt) {
    System.out.println(evt);
  }
  
  public void start() {
    final Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        while (isRunning) {

          pollAllTopicsForAllClients();
          
          try {
            Thread.sleep(POLLING_DELAY);
          } catch (InterruptedException e) {
            throw new RuntimeException("thread was interrupted", e);
          }
        }
      }    
    });
    isRunning = true;
    thread.start();
  }

  public void stop() {
    isRunning = false;
    try {
      Thread.sleep(POLLING_DELAY+1);
    } catch (InterruptedException e) {
    }
    // TODO: Block until stopped
  }
}
