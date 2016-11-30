package com.camunda.consulting.tngp.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import com.camunda.consulting.tngp.dto.BrokerConnectionDto;
import com.camunda.consulting.tngp.dto.WorkflowDefinitionDto;
import com.camunda.consulting.tngp.dto.WorkflowInstanceDto;
import com.camunda.consulting.tngp.rest.BrokerResource;
import com.camunda.consulting.tngp.rest.WorkflowDefinitionResource;
import com.camunda.consulting.tngp.rest.WorkflowInstanceResource;

public class TngpEventPolling {

  protected static final long POLLING_DELAY = 500;
  
  private Map<TngpClient, Map<Integer, Long>> tngpClients = new HashMap<TngpClient, Map<Integer, Long>>();
  
  public static Map<String, List<String>> events = new HashMap<String, List<String>>();
  
  private String getBrokerName(TngpClient client) {
    BrokerConnectionDto connection = BrokerResource.getBrokerConnection(client);
    if (connection!=null) {
      return connection.getConnectionString();
    } else {
      return "default";
    }
  }
  
  public List<String> getEvents(TngpClient client) {
    List<String> list = events.get(getBrokerName(client));
    if (list==null) {
      list = new ArrayList<>();
      events.put(getBrokerName(client), list);
    }
    return list;
  }
  
  public void connectTngpClient(TngpClient client) {
    client.connect();
    HashMap<Integer, Long> positions = new HashMap<Integer, Long>();
    positions.put(0, 0l);
    positions.put(1, 0l);
    positions.put(2, 0l);
    tngpClients.put(client, positions);
    events.put(getBrokerName(client), new ArrayList<>());
  }

  public void disconnectTngpClient(TngpClient client) {
    tngpClients.remove(client);    
    client.disconnect();
    client.close();
    
    WorkflowDefinitionResource.removeBrokerData(client);
    events.remove(getBrokerName(client));
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
        handle(tngpClient, (TaskInstanceEventImpl)evt);        
      }
      else if (evt instanceof WorkflowDefinitionRequestEventImpl) {
        handle(tngpClient, (WorkflowDefinitionRequestEventImpl)evt);        
      }
      // Events not yet parsed in client lib, decode ourselves
      else if (evt instanceof UnknownEvent) {
        MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder().wrap(evt.getRawBuffer(), 0);
        
        switch (((UnknownEvent) evt).getTemplateId()) {
        case BpmnProcessEventDecoder.TEMPLATE_ID: {
          BpmnProcessEventDecoder decoder = new BpmnProcessEventDecoder() //
            .wrap(evt.getRawBuffer(), 0 + headerDecoder.encodedLength(), headerDecoder.blockLength(), headerDecoder.version());          
          handle(tngpClient, decoder);
          break;
        }
        case BpmnActivityEventDecoder.TEMPLATE_ID: {
          BpmnActivityEventDecoder decoder = new BpmnActivityEventDecoder() //
            .wrap(evt.getRawBuffer(), 0 + headerDecoder.encodedLength(), headerDecoder.blockLength(), headerDecoder.version());          
          handle(tngpClient, decoder);
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
          handle(tngpClient, decoder);
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
          handle(tngpClient, decoder);
          break;
        }
        case TaskInstanceRequestDecoder.TEMPLATE_ID: {
          TaskInstanceRequestDecoder decoder = new TaskInstanceRequestDecoder() //
              .wrap(evt.getRawBuffer(), 0 + headerDecoder.encodedLength(), headerDecoder.blockLength(), headerDecoder.version());          
          handle(tngpClient, decoder);
          break;
        }
        case CreateTaskRequestDecoder.TEMPLATE_ID: {
          CreateTaskRequestDecoder decoder = new CreateTaskRequestDecoder() //
              .wrap(evt.getRawBuffer(), 0 + headerDecoder.encodedLength(), headerDecoder.blockLength(), headerDecoder.version());          
          handle(tngpClient, decoder);
          break;
        }
        }

      }
      else {
        System.out.println("Event of type " + evt.getClass() + " unkown: " + evt);
      }
    }
  }



  private void handle(TngpClient client, CreateTaskRequestDecoder decoder) {
    getEvents(client).add(decoder.toString());
  }

  private void handle(TngpClient client, TaskInstanceRequestDecoder decoder) {
    getEvents(client).add(decoder.toString());
  }

  private void handle(TngpClient client, ActivityInstanceRequestDecoder decoder) {
    getEvents(client).add(decoder.toString());
  }

  private void handle(TngpClient client, BpmnProcessEventDecoder decoder) {
    getEvents(client).add(decoder.toString());
    
    long key=decoder.key(); // identifier for "local state machine" / compare to primary key
    int event=decoder.event();
    long wfDefinitionId=decoder.wfDefinitionId();
    long wfInstanceId=decoder.wfInstanceId();
    int initialElementId=decoder.initialElementId();
    long bpmnBranchKey=decoder.bpmnBranchKey();
    
    if (event==0) { // created, see https://github.com/camunda-tngp/compact-graph-bpmn/blob/master/src/main/resources/schema.xml#L58
      WorkflowInstanceDto dto = new WorkflowInstanceDto();
      
      dto.setId(wfInstanceId);
      dto.setWorkflowDefinitionId(wfDefinitionId);
//      dto.workflowDefinitionKey = decoder.key;
     
      WorkflowInstanceResource.newWorkflowInstanceStarted(client, dto);
    }else if (event==1) { // completed
      WorkflowInstanceResource.setEnded(client, wfInstanceId);      
    }
  }

  private void handle(TngpClient client, WorkflowInstanceRequestDecoder decoder) {
    getEvents(client).add(decoder.toString());
  }

  private void handle(TngpClient client, TaskInstanceDecoder decoder) {
    getEvents(client).add(decoder.toString());
  }

  private void handle(TngpClient client, BpmnFlowElementEventDecoder decoder) {
    getEvents(client).add(decoder.toString());
    
    long key=decoder.key();
    int event=decoder.event();
    long wfDefinitionId=decoder.wfDefinitionId();
    long wfInstanceId=decoder.wfInstanceId();
    int flowElementId=decoder.flowElementId();
    long bpmnBranchKey=decoder.bpmnBranchKey();
    String flowElementIdString=decoder.flowElementIdString();
    String payload=decoder.payload();
    
    // flow elements which are not activities might be interesting for history
    WorkflowInstanceResource.addActivityEnded(client, 
        wfInstanceId,
        flowElementIdString,
        payload);          
  }

  private void handle(TngpClient client, BpmnActivityEventDecoder decoder) {
    getEvents(client).add(decoder.toString());
    
    long key = decoder.key();
    int event=decoder.event();
    long wfDefinitionId=decoder.wfDefinitionId();
    long wfInstanceId=decoder.wfInstanceId();
    int flowElementId=decoder.flowElementId();
    int taskQueueId=decoder.taskQueueId();
    long bpmnBranchKey=decoder.bpmnBranchKey();
    String taskType=decoder.taskType();
    String flowElementIdString=decoder.flowElementIdString();
    String payload=decoder.payload();

    if (event==200) { // created, see https://github.com/camunda-tngp/compact-graph-bpmn/blob/master/src/main/resources/schema.xml
      WorkflowInstanceResource.addActivityStarted(client, 
          wfInstanceId, 
          flowElementIdString, 
          payload);
      
    } else if (event==201) { // completed
      WorkflowInstanceResource.addActivityEnded(client, 
          wfInstanceId,
          flowElementIdString,
          payload);      
    }
  }

  private void handle(TngpClient client, TaskInstanceEventImpl evt) {
    getEvents(client).add(evt.toString());
  }

  private void handle(TngpClient client, WorkflowDefinitionEventImpl evt) {
    getEvents(client).add(evt.toString());
    WorkflowDefinitionResource.add(
        client,
        WorkflowDefinitionDto.from(evt));
  }
  
  private void handle(TngpClient client, WorkflowDefinitionRequestEventImpl evt) {
    getEvents(client).add(evt.toString());
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