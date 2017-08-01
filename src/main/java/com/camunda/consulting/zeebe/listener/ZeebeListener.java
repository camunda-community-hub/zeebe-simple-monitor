package com.camunda.consulting.zeebe.listener;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.camunda.consulting.zeebe.Constants;
import com.camunda.consulting.zeebe.dto.BrokerConnectionDto;
import com.camunda.consulting.zeebe.dto.WorkflowDefinitionDto;
import com.camunda.consulting.zeebe.dto.WorkflowInstanceDto;
import com.camunda.consulting.zeebe.rest.BrokerResource;
import com.camunda.consulting.zeebe.rest.WorkflowDefinitionResource;
import com.camunda.consulting.zeebe.rest.WorkflowInstanceResource;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.event.TopicEventType;
import io.zeebe.client.event.impl.TopicEventImpl;
import io.zeebe.client.impl.data.MsgPackConverter;
import io.zeebe.client.impl.data.MsgPackMapper;
import io.zeebe.client.task.impl.TaskEvent;
import io.zeebe.client.task.impl.subscription.TaskImpl;
import io.zeebe.client.workflow.impl.WorkflowInstanceEvent;


public class ZeebeListener {

  protected static final long POLLING_DELAY = 500;
  
  public ObjectMapper objectMapper = new ObjectMapper();
  public MsgPackMapper msgPackMapper = new MsgPackMapper(objectMapper);
  public MsgPackConverter msgPackConverter = new MsgPackConverter();
  
  private List<ZeebeClient> clients = new ArrayList<ZeebeClient>();
  
  /**
   * Map
   * - broker name -> Map
   *   - topic id -> List 
   *     - event string representation
   */
  public static Map<String, Map<Integer, List<String>>> events = new HashMap<String, Map<Integer, List<String>>>();
  
  private String getBrokerName(ZeebeClient client) {
    BrokerConnectionDto connection = BrokerResource.getBrokerConnection(client);
    if (connection!=null) {
      return connection.getConnectionString();
    } else {
      return "default";
    }
  }
  
  public Map<Integer, List<String>> getEvents(ZeebeClient client) {
    Map<Integer, List<String>> map = events.get(getBrokerName(client));
    if (map==null){
      map = new HashMap<>();
      events.put(getBrokerName(client), map);      
    }
    return map;
  }
  public List<String> getEvents(ZeebeClient client, int topicId) {
    Map<Integer, List<String>> map = getEvents(client);
    List<String> list = map.get(topicId);
    if (list==null) {
      list = new ArrayList<>();
      map.put(topicId, list);
    }
    return list;
  }
  
  public void connectTngpClient(ZeebeClient client) {
    System.out.println("Opening subscription");
    client.topic(Constants.DEFAULT_TOPIC, Constants.DEFAULT_PARTITION)
        .newSubscription()
        .startAtHeadOfTopic().forcedStart()
        .name("zeebe-simple-monitor")
        .handler((meta, event) ->
        {
            System.out.println(String.format(">>> [topic: %d, position: %d, key: %d, type: %s]\n%s\n===",
                    meta.getPartitionId(),
                    meta.getEventPosition(),
                    meta.getEventKey(),
                    meta.getEventType(),
                    event.getJson()));
            
            JsonReader jsonReader = Json.createReader(new StringReader(event.getJson()));
            JsonObject eventJson = jsonReader.readObject();

            String eventType = eventJson.getString("eventType", null);            
            
            if (TopicEventType.WORKFLOW_INSTANCE.equals(meta.getEventType())) {              
              if ("WORKFLOW_INSTANCE_CREATED".equals(eventType)) {                
                final WorkflowInstanceEvent workflowInstanceEvent = msgPackMapper.convert(((TopicEventImpl)event).getAsMsgPack(), WorkflowInstanceEvent.class);

                WorkflowInstanceResource.newWorkflowInstanceStarted(
                    client, 
                    WorkflowInstanceDto.from(workflowInstanceEvent));
                System.out.println("Workflow instance started");
              }              
            }
            if (meta.getEventType()==null) {
              if ("DEPLOYMENT_CREATED".equals(eventType)) {                
                WorkflowDefinitionResource.addAll(client, WorkflowDefinitionDto.from(eventJson));
                System.out.println("Workflow deployed");
              }
              
            }
//            WorkflowInstanceResource.setEnded(client, workflowInstanceId);
//            WorkflowInstanceResource.addActivityEnded(client, wfInstanceId, flowElementIdString, payload);
//            WorkflowInstanceResource.addActivityStarted(client, wfInstanceId, flowElementIdString, payload);
        })
    .open();
    System.out.println("Done");
    clients.add(client);
  }

  public void disconnectTngpClient(ZeebeClient client) {
    clients.remove(client);    
    client.disconnect();
    client.close();
    
    WorkflowDefinitionResource.removeBrokerData(client);
    events.remove(getBrokerName(client));
  }

}
