package com.camunda.consulting.zeebe.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.camunda.consulting.zeebe.Constants;
import com.camunda.consulting.zeebe.dto.BrokerConnectionDto;
import com.camunda.consulting.zeebe.dto.WorkflowDefinitionDto;
import com.camunda.consulting.zeebe.dto.WorkflowInstanceDto;
import com.camunda.consulting.zeebe.rest.BrokerResource;
import com.camunda.consulting.zeebe.rest.WorkflowDefinitionResource;
import com.camunda.consulting.zeebe.rest.WorkflowInstanceResource;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.event.EventMetadata;


public class ZeebeListener {

  protected static final long POLLING_DELAY = 500;
  
//  public ObjectMapper objectMapper = new ObjectMapper();
//  public MsgPackMapper msgPackMapper = new MsgPackMapper(objectMapper);
//  public MsgPackConverter msgPackConverter = new MsgPackConverter();
  
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
//    final ObjectMapper objectMapper = new ObjectMapper();
//    objectMapper.setInjectableValues(new InjectableValues.Std().addValue(MsgPackConverter.class, new MsgPackConverter()));
    
    client.topics()
        .newSubscription(Constants.DEFAULT_TOPIC)
        .startAtHeadOfTopic().forcedStart()
        .name("zeebe-simple-monitor")
        .incidentEventHandler((event) -> {          
          if ("CREATED".equals(event.getState())) {
            WorkflowInstanceResource.addIncident(client, event.getWorkflowInstanceKey(), event.getActivityId(), event.getErrorType(), event.getErrorMessage());            
          }
        })
        .workflowInstanceEventHandler((event) -> {
          // WorkflowInstanceState.XXX
          if ("WORKFLOW_INSTANCE_CREATED".equals(event.getState())) {
            WorkflowInstanceResource.newWorkflowInstanceStarted(client, WorkflowInstanceDto.from(event));
          }        
          if ("WORKFLOW_INSTANCE_COMPLETED".equals(event.getState())) {
            WorkflowInstanceResource.setEnded(client, event.getWorkflowInstanceKey());
          }
          if ("WORKFLOW_INSTANCE_CANCELED".equals(event.getState())) {
            WorkflowInstanceResource.setCanceled(client, event.getWorkflowInstanceKey());
          }
          if ("WORKFLOW_INSTANCE_CANCELED".equals(event.getState())) {
            WorkflowInstanceResource.setCanceled(client, event.getWorkflowInstanceKey());
          }
          if ("ACTIVITY_ACTIVATED".equals(event.getState())) {
            WorkflowInstanceResource.addActivityStarted(client, event.getWorkflowInstanceKey(), event.getActivityId(), event.getPayload());
          }
          if ("ACTIVITY_COMPLETED".equals(event.getState())) {
            WorkflowInstanceResource.addActivityEnded(client, event.getWorkflowInstanceKey(), event.getActivityId(), event.getPayload());
          }
          if ("ACTIVITY_TERMINATED".equals(event.getState())) {
            WorkflowInstanceResource.addActivityEnded(client, event.getWorkflowInstanceKey(), event.getActivityId(), event.getPayload());
          }           
          /**
          START_EVENT_OCCURRED,
          END_EVENT_OCCURRED,
          */
        })        
        .workflowEventHandler((event) -> {
          // Feebdack: Expose constant in Client API
          // WorkflowState.CREATED                
          if ("CREATED".equals(event.getState())) {                
            WorkflowDefinitionResource.add(client, WorkflowDefinitionDto.from(event));
            System.out.println("Workflow deployed");
          }
        })          
        .handler((event) ->
        {
          final EventMetadata metadata = event.getMetadata();
          System.out.println(String.format(">>> [topic: %d, position: %d, key: %d, type: %s]\n%s\n===",
                  metadata.getPartitionId(),
                  metadata.getPosition(),
                  metadata.getKey(),
                  metadata.getType(),
                  event.getJson()));
            
//            if (TopicEventType.WORKFLOW_INSTANCE.equals(event.getMetadata().getType())) {   
//                final WorkflowInstanceEventImpl workflowInstanceEvent = objectMapper.readValue(event.getJson(), WorkflowInstanceEventImpl.class);                
//                workflowInstanceEvent.updateMetadata(event.getMetadata());
//            }
//            if (TopicEventType.DEPLOYMENT.equals(event.getMetadata().getType())) {              
//              final DeploymentEventImpl deploymentEvent = objectMapper.readValue(event.getJson(), DeploymentEventImpl.class);                
//              deploymentEvent.updateMetadata(event.getMetadata());              
//            }           
        })
    .open();
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
