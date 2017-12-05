/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.consulting.zeebemonitor.zeebe;

import java.io.StringReader;
import java.util.*;
import javax.json.Json;

import com.camunda.consulting.zeebemonitor.Constants;
import com.camunda.consulting.zeebemonitor.entity.*;
import com.camunda.consulting.zeebemonitor.repository.*;
import io.zeebe.client.ClientProperties;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.event.IncidentEvent;
import io.zeebe.client.event.WorkflowInstanceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

@Component
@ApplicationScope
public class ZeebeConnections
{

    private static final Set<String> ACTIVITY_END_STATES;

    static
    {
        ACTIVITY_END_STATES = new HashSet<>();
        ACTIVITY_END_STATES.add("ACTIVITY_COMPLETED");
        ACTIVITY_END_STATES.add("ACTIVITY_TERMINATED");

        ACTIVITY_END_STATES.add("GATEWAY_ACTIVATED");

        ACTIVITY_END_STATES.add("START_EVENT_OCCURRED");
        ACTIVITY_END_STATES.add("END_EVENT_OCCURRED");
    }

    @Autowired
    private WorkflowDefinitionRepository workflowDefinitionRepository;
    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    @Autowired
    private IncidentRepository incidentRepository;
    @Autowired
    private LoggedEventRepository loggedEventRepository;
    @Autowired
    private BrokerRepository brokerRepository;

    /**
     * broker connectionString -> ZeebeClirnt
     */
    private Map<String, ZeebeClient> openConnections = new HashMap<>();

    public ArrayList<ZeebeConnectionDto> getConnectionDtoList()
    {
        final Iterable<Broker> allBrokers = brokerRepository.findAll();
        final ArrayList<ZeebeConnectionDto> result = new ArrayList<>();
        for (Broker broker : allBrokers)
        {
            result.add(getConnectionDto(broker));
        }
        return result;
    }

    public ZeebeConnectionDto getConnectionDto(Broker broker)
    {
        return new ZeebeConnectionDto(broker, isConnected(broker));
    }

    public ZeebeClient connect(final Broker broker)
    {
        final Properties clientProperties = new Properties();
        clientProperties.put(ClientProperties.BROKER_CONTACTPOINT, broker.getConnectionString());

        final ZeebeClient client = ZeebeClient.create(clientProperties);

        openConnections.put(broker.getConnectionString(), client);

        // TODO: Think about the use case when connecting to various brokers on localhost
        final String clientName = "zeebe-simple-monitor";
        final String typedSubscriptionName = clientName + "-typed";
        final String untypedSubscriptionName = clientName + "-untyped";

        client.topics().newSubscription(Constants.DEFAULT_TOPIC) //
              .startAtHeadOfTopic() //
              .forcedStart() //
              .name(typedSubscriptionName).incidentEventHandler((event) ->
              {
                  if ("CREATED".equals(event.getState()))
                  {
                      workflowInstanceIncidentOccured(broker, event);
                  }
                  if ("RESOLVE_FAILED".equals(event.getState()))
                  {
                      workflowInstanceIncidentUpdated(broker, event);
                  }
                  if ("RESOLVED".equals(event.getState()) || "DELETED".equals(event.getState()))
                  {
                      workflowInstanceIncidentResolved(broker, event);
                  }
              })
              .workflowInstanceEventHandler((event) ->
              {
                  // WorkflowInstanceState.XXX
                  if ("WORKFLOW_INSTANCE_CREATED".equals(event.getState()))
                  {
                      workflowInstanceStarted(broker, WorkflowInstance.from(event));
                  }
                  if ("WORKFLOW_INSTANCE_COMPLETED".equals(event.getState()))
                  {
                      workflowInstanceEnded(broker, event.getWorkflowInstanceKey());
                  }
                  if ("WORKFLOW_INSTANCE_CANCELED".equals(event.getState()))
                  {
                      workflowInstanceCanceled(broker, event.getWorkflowInstanceKey());
                  }
                  if ("ACTIVITY_READY".equals(event.getState()))
                  {
                      workflowInstanceUpdated(broker, event);
                  }
                  if ("ACTIVITY_ACTIVATED".equals(event.getState()))
                  {
                      workflowInstanceActivityStarted(broker, event);
                  }
                  if ("ACTIVITY_COMPLETING".equals(event.getState()))
                  {
                      workflowInstanceUpdated(broker, event);
                  }
                  if (ACTIVITY_END_STATES.contains(event.getState()))
                  {
                      workflowInstanceActivityEnded(broker, event);
                  }
                  if ("SEQUENCE_FLOW_TAKEN".equals(event.getState()))
                  {
                      sequenceFlowTaken(broker, event);
                  }
                  if ("PAYLOAD_UPDATED".equals(event.getState()))
                  {
                      workflowInstancePayloadUpdated(broker, event);
                  }
              })
              .workflowEventHandler((event) ->
              {
                  // Feebdack: Expose constant in Client API
                  // WorkflowState.CREATED
                  if ("CREATED".equals(event.getState()))
                  {
                      workflowDefinitionDeployed(broker, WorkflowDefinition.from(event));
                  }
              })
              .open();

        client.topics().newSubscription(Constants.DEFAULT_TOPIC).startAtHeadOfTopic().forcedStart().name(untypedSubscriptionName).handler((event) ->
        {
            final String state = Json.createReader(new StringReader(event.getJson())).readObject().getString("state");
            loggedEventRepository.save(new LoggedEvent(//
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

    private void workflowDefinitionDeployed(Broker broker, WorkflowDefinition def)
    {
        def.setBroker(broker);
        workflowDefinitionRepository.save(def);
    }

    private void workflowInstanceStarted(Broker broker, WorkflowInstance instance)
    {
        instance.setBroker(broker);
        workflowInstanceRepository.save(instance);
    }

    private void workflowInstanceEnded(Broker broker, long workflowInstanceKey)
    {
        workflowInstanceRepository.save(workflowInstanceRepository.findOne(workflowInstanceKey) //
                                                                  .setEnded(true));
    }

    private void workflowInstanceActivityStarted(Broker broker, WorkflowInstanceEvent event)
    {
        workflowInstanceRepository.save(workflowInstanceRepository.findOne(event.getWorkflowInstanceKey()) //
                                                                  .activityStarted(event.getActivityId(), event.getPayload())
                                                                  .setLastEventPosition(event.getMetadata().getPosition()));
    }

    private void workflowInstanceActivityEnded(Broker broker, WorkflowInstanceEvent event)
    {
        workflowInstanceRepository.save(//
                workflowInstanceRepository.findOne(event.getWorkflowInstanceKey()) //
                                          .activityEnded(event.getActivityId(), event.getPayload()).setLastEventPosition(event.getMetadata().getPosition()));
    }

    private void workflowInstanceCanceled(Broker broker, long workflowInstanceKey)
    {
        workflowInstanceRepository.save(//
                workflowInstanceRepository.findOne(workflowInstanceKey) //
                                          .setEnded(true));
    }

    private void workflowInstancePayloadUpdated(Broker broker, WorkflowInstanceEvent event)
    {
        workflowInstanceRepository.save(//
                workflowInstanceRepository.findOne(event.getWorkflowInstanceKey()) //
                                          .setPayload(event.getPayload()));
    }

    private void workflowInstanceUpdated(Broker broker, WorkflowInstanceEvent event)
    {
        workflowInstanceRepository.save(//
                workflowInstanceRepository.findOne(event.getWorkflowInstanceKey()) //
                                          .setPayload(event.getPayload()).setLastEventPosition(event.getMetadata().getPosition()));
    }

    private void workflowInstanceIncidentOccured(Broker broker, IncidentEvent event)
    {
        final Incident incident = new Incident(event.getMetadata().getKey(), event.getActivityId(), event.getErrorType(), event.getErrorMessage());

        incidentRepository.save(incident);

        workflowInstanceRepository.save(//
                workflowInstanceRepository.findOne(event.getWorkflowInstanceKey()) //
                                          .incidentOccured(incident));
    }

    private void workflowInstanceIncidentUpdated(Broker broker, IncidentEvent event)
    {
        final Incident incident = incidentRepository.findOne(event.getMetadata().getKey());

        if (incident != null)
        {
            incidentRepository.save(//
                    incident.setErrorType(event.getErrorType()).setErrorMessage(event.getErrorMessage()));
        }
    }

    private void workflowInstanceIncidentResolved(Broker broker, IncidentEvent event)
    {
        final Incident incident = incidentRepository.findOne(event.getMetadata().getKey());

        workflowInstanceRepository.save(//
                workflowInstanceRepository.findOne(event.getWorkflowInstanceKey()) //
                                          .incidentResolved(incident));

        incidentRepository.delete(incident);
    }

    private void sequenceFlowTaken(Broker broker, WorkflowInstanceEvent event)
    {
        workflowInstanceRepository.save(//
                workflowInstanceRepository.findOne(event.getWorkflowInstanceKey()) //
                                          .sequenceFlowTaken(event.getActivityId()));
    }

    public void disconnect(Broker broker)
    {
        final ZeebeClient client = openConnections.get(broker.getConnectionString());

        client.disconnect();
        client.close();

        openConnections.remove(broker.getConnectionString());
    }

    public void deleteAllData()
    {
        for (ZeebeClient client : openConnections.values())
        {
            client.disconnect();
            client.close();
        }
        openConnections = new HashMap<>();

        workflowInstanceRepository.deleteAll();
        workflowDefinitionRepository.deleteAll();
        loggedEventRepository.deleteAll();
        brokerRepository.deleteAll();
    }

    public ZeebeClient getZeebeClient(String brokerConnectionString)
    {
        return openConnections.get(brokerConnectionString);
    }

    public ZeebeClient getZeebeClient(Broker broker)
    {
        return openConnections.get(broker.getConnectionString());
    }

    public boolean isConnected(Broker broker)
    {
        return openConnections.containsKey(broker.getConnectionString());
    }

}
