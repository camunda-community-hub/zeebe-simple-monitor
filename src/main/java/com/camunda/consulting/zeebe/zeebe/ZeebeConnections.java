package com.camunda.consulting.zeebe.zeebe;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.json.Json;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import com.camunda.consulting.zeebe.Constants;
import com.camunda.consulting.zeebe.entity.Broker;
import com.camunda.consulting.zeebe.entity.Incident;
import com.camunda.consulting.zeebe.entity.LoggedEvent;
import com.camunda.consulting.zeebe.entity.WorkflowDefinition;
import com.camunda.consulting.zeebe.entity.WorkflowInstance;
import com.camunda.consulting.zeebe.repository.BrokerRepository;
import com.camunda.consulting.zeebe.repository.LoggedEventRepository;
import com.camunda.consulting.zeebe.repository.WorkflowDefinitionRepository;
import com.camunda.consulting.zeebe.repository.WorkflowInstanceRepository;

import io.zeebe.client.ClientProperties;
import io.zeebe.client.ZeebeClient;

@Component
@ApplicationScope
public class ZeebeConnections {

  @Autowired
  private WorkflowDefinitionRepository workflowDefinitionRepository;
  @Autowired
  private WorkflowInstanceRepository workflowInstanceRepository;
  @Autowired
  private LoggedEventRepository loggedEventRepository;
  @Autowired
  private BrokerRepository brokerRepository;

  /**
   * broker connectionString -> ZeebeClirnt
   */
  private Map<String, ZeebeClient> openConnections = new HashMap<String, ZeebeClient>();

  public ArrayList<ZeebeConnectionDto> getConnectionDtoList() {
    Iterable<Broker> allBrokers = brokerRepository.findAll();
    ArrayList<ZeebeConnectionDto> result = new ArrayList<ZeebeConnectionDto>();
    for (Broker broker : allBrokers) {
      result.add(getConnectionDto(broker));
    }
    return result;    
  }
  
  public ZeebeConnectionDto getConnectionDto(Broker broker) {
    return new ZeebeConnectionDto(broker, isConnected(broker));
  }

  public ZeebeClient connect(final Broker broker) {
    Properties clientProperties = new Properties();
    clientProperties.put(ClientProperties.BROKER_CONTACTPOINT, broker.getConnectionString());

    ZeebeClient client = ZeebeClient.create(clientProperties);
    client.connect();
    
    openConnections.put(broker.getConnectionString(), client);

    // TODO: Think about the use case when connecting to various brokers on localhost
    String clientName = UUID.randomUUID().toString().substring(0, 31); // "zeebe-simple-monitor";

    client.topics().newSubscription(Constants.DEFAULT_TOPIC) //
        .startAtHeadOfTopic() //
        .forcedStart() //
        .name(clientName).incidentEventHandler((event) -> {
          if ("CREATED".equals(event.getState())) {
            workflowInstanceIncidentOccured(broker, event.getWorkflowInstanceKey(), event.getActivityId(), event.getErrorType(), event.getErrorMessage());
          }
        }).workflowInstanceEventHandler((event) -> {
          // WorkflowInstanceState.XXX
          if ("WORKFLOW_INSTANCE_CREATED".equals(event.getState())) {
            workflowInstanceStarted(broker, WorkflowInstance.from(event));
          }
          if ("WORKFLOW_INSTANCE_COMPLETED".equals(event.getState())) {
            workflowInstanceEnded(broker, event.getWorkflowInstanceKey());
          }
          if ("WORKFLOW_INSTANCE_CANCELED".equals(event.getState())) {
            workflowInstanceCanceled(broker, event.getWorkflowInstanceKey());
          }
          if ("ACTIVITY_ACTIVATED".equals(event.getState())) {
            workflowInstanceActivityStarted(broker, event.getWorkflowInstanceKey(), event.getActivityId(), event.getPayload());
          }
          if ("ACTIVITY_COMPLETED".equals(event.getState())) {
            workflowInstanceActivityEnded(broker, event.getWorkflowInstanceKey(), event.getActivityId(), event.getPayload());
          }
          if ("ACTIVITY_TERMINATED".equals(event.getState())) {
            workflowInstanceActivityEnded(broker, event.getWorkflowInstanceKey(), event.getActivityId(), event.getPayload());
          }
          /**
           * START_EVENT_OCCURRED, END_EVENT_OCCURRED,
           */
        }).workflowEventHandler((event) -> {
          // Feebdack: Expose constant in Client API
          // WorkflowState.CREATED
          if ("CREATED".equals(event.getState())) {
            workflowDefinitionDeployed(broker, WorkflowDefinition.from(event));
          }
        }).handler((event) -> {

          String state = Json.createReader(new StringReader(event.getJson())).readObject().getString("state");
          loggedEventRepository.save(new LoggedEvent( //
              broker, //
              event.getMetadata().getPartitionId(), //
              event.getMetadata().getPosition(), //
              event.getMetadata().getKey(), //
              event.getMetadata().getType().toString(), //
              // event.getState(), // WAIT FOR
              // https://github.com/zeebe-io/zeebe/issues/367
              state, event.getJson()));
        }).open();
    return client;
  }

  private void workflowDefinitionDeployed(Broker broker, WorkflowDefinition def) {
    def.setBroker(broker);
    workflowDefinitionRepository.save(def);
  }

  private void workflowInstanceStarted(Broker broker, WorkflowInstance instance) {
    instance.setBroker(broker);
    workflowInstanceRepository.save(instance);
  }

  private void workflowInstanceEnded(Broker broker, long workflowInstanceKey) {
    workflowInstanceRepository.save(workflowInstanceRepository.findOne(workflowInstanceKey) //
        .setEnded(true));
  }

  private void workflowInstanceActivityStarted(Broker broker, long workflowInstanceKey, String activityId, String payload) {
    workflowInstanceRepository.save(workflowInstanceRepository.findOne(workflowInstanceKey) //
        .activityStarted(activityId, payload));
  }

  private void workflowInstanceActivityEnded(Broker broker, long workflowInstanceKey, String activityId, String payload) {
    workflowInstanceRepository.save( //
        workflowInstanceRepository.findOne(workflowInstanceKey) //
            .activityEnded(activityId, payload));
  }

  public void workflowInstanceCanceled(Broker broker, long workflowInstanceKey) {
    workflowInstanceRepository.save( //
        workflowInstanceRepository.findOne(workflowInstanceKey) //
            .setEnded(true));
  }

  private void workflowInstanceIncidentOccured(Broker broker, Long workflowInstanceKey, String activityId, String errorType, String errorMessage) {
    workflowInstanceRepository.save( //
        workflowInstanceRepository.findOne(workflowInstanceKey) //
            .incidentOccured(new Incident(activityId, errorType, errorMessage)));
  }

  public void disconnect(Broker broker) {
    ZeebeClient client = openConnections.get(broker.getConnectionString());

    client.disconnect();
    client.close();
    
    openConnections.remove(broker.getConnectionString());
  }

  public void deleteAllData() {
    for (ZeebeClient client : openConnections.values()) {
      client.disconnect();
      client.close();
    }
    openConnections = new HashMap<>();
    
    workflowInstanceRepository.deleteAll();
    workflowDefinitionRepository.deleteAll();
    loggedEventRepository.deleteAll();
    brokerRepository.deleteAll();    
  }

  public ZeebeClient getZeebeClient(String brokerConnectionString) {
    return openConnections.get(brokerConnectionString);
  }

  public ZeebeClient getZeebeClient(Broker broker) {
    return openConnections.get(broker.getConnectionString());
  }

  public boolean isConnected(Broker broker) {
    return openConnections.containsKey(broker.getConnectionString());
  }

}
