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
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.event.EventMetadata;
import io.zeebe.client.event.TopicEventType;
import io.zeebe.client.impl.data.MsgPackConverter;
import io.zeebe.client.impl.data.MsgPackMapper;
import io.zeebe.client.workflow.impl.DeploymentEventImpl;
import io.zeebe.client.workflow.impl.WorkflowInstanceEventImpl;


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
    
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setInjectableValues(new InjectableValues.Std().addValue(MsgPackConverter.class, new MsgPackConverter()));
    
    client.topics()
        .newSubscription(Constants.DEFAULT_TOPIC)
        .startAtHeadOfTopic().forcedStart()
        .name("zeebe-simple-monitor")
        .handler((event) ->
        {
          final EventMetadata metadata = event.getMetadata();
          System.out.println(String.format(">>> [topic: %d, position: %d, key: %d, type: %s]\n%s\n===",
                  metadata.getPartitionId(),
                  metadata.getPosition(),
                  metadata.getKey(),
                  metadata.getType(),
                  event.getJson()));
            
            if (TopicEventType.WORKFLOW_INSTANCE.equals(event.getMetadata().getType())) {   
                final WorkflowInstanceEventImpl workflowInstanceEvent = objectMapper.readValue(event.getJson(), WorkflowInstanceEventImpl.class);                
                workflowInstanceEvent.updateMetadata(event.getMetadata());
                if ("WORKFLOW_INSTANCE_CREATED".equals(workflowInstanceEvent.getState())) {
                  WorkflowInstanceResource.newWorkflowInstanceStarted(
                      client, 
                      WorkflowInstanceDto.from(workflowInstanceEvent));
                  System.out.println("Workflow instance started");
                }              
            }
            if (TopicEventType.DEPLOYMENT.equals(event.getMetadata().getType())) {              
              final DeploymentEventImpl deploymentEvent = objectMapper.readValue(event.getJson(), DeploymentEventImpl.class);                
              deploymentEvent.updateMetadata(event.getMetadata());
              // Feebdack: Expose constant in Client API
              //if (DeploymentState.DEPLOYMENT_CREATED.equals(eventState)) {                
              if ("DEPLOYMENT_CREATED".equals(deploymentEvent.getState())) {                
                WorkflowDefinitionResource.addAll(client, WorkflowDefinitionDto.from(deploymentEvent));
                System.out.println("Workflow deployed");
              }
              
            }
            // TODO: add more vents
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
