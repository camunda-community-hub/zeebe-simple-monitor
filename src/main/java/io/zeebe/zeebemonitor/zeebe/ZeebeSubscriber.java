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
package io.zeebe.zeebemonitor.zeebe;

import java.util.Optional;

import io.zeebe.client.api.events.IncidentEvent;
import io.zeebe.client.api.events.WorkflowInstanceEvent;
import io.zeebe.client.api.record.Record;
import io.zeebe.client.api.record.RecordMetadata;
import io.zeebe.client.api.subscription.*;
import io.zeebe.client.api.subscription.TopicSubscriptionBuilderStep1.TopicSubscriptionBuilderStep3;
import io.zeebe.zeebemonitor.entity.*;
import io.zeebe.zeebemonitor.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ZeebeSubscriber
{
    private static final Logger LOG = LoggerFactory.getLogger(ZeebeSubscriber.class);

    private static final String SUBSCRIPTION_NAME = "zeebe-simple-monitor";

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Autowired
    private RecordRepository loggedEventRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private ZeebeConnectionService connectionService;

    public void openSubscription(String topicName)
    {
        LOG.debug("open subscription of topic {}", topicName);

        final Optional<SubscriptionEntity> subscriptionEntity = subscriptionRepository.findById(topicName);
        final String subscriptionName = subscriptionEntity.map(SubscriptionEntity::getSubscriptionName).orElse(SUBSCRIPTION_NAME);

        final Handler handler = new Handler();

        final TopicSubscriptionBuilderStep3 subscriptionBuilder = connectionService
                .getClient()
                .topicClient(topicName)
                .newSubscription()
                .name(subscriptionName)
                .workflowInstanceEventHandler(handler::onWorkflowInstanceEvent)
                .incidentEventHandler(handler::onIncidentEvent)
                .recordHandler(handler::onRecord)
                .startAtHeadOfTopic();

        if (!subscriptionEntity.isPresent())
        {
            subscriptionBuilder.forcedStart();

            final SubscriptionEntity newSubscriptionEntity = new SubscriptionEntity();
            newSubscriptionEntity.setTopicName(topicName);
            newSubscriptionEntity.setSubscriptionName(subscriptionName);

            subscriptionRepository.save(newSubscriptionEntity);
        }

        try
        {
            subscriptionBuilder.open();
        }
        catch (Exception e)
        {
            LOG.error("Failed to open subscription", e);
        }
    }

    private class Handler implements WorkflowInstanceEventHandler, IncidentEventHandler, RecordHandler
    {
        @Override
        public void onRecord(Record record) throws Exception
        {
            insertRecord(record);
        }

        @Override
        public void onWorkflowInstanceEvent(WorkflowInstanceEvent event) throws Exception
        {
            switch (event.getState())
            {
                case CREATED:
                    workflowInstanceStarted(WorkflowInstanceEntity.from(event));
                    break;

                case COMPLETED:
                    workflowInstanceEnded(event);
                    break;

                case CANCELED:
                    workflowInstanceCanceled(event);
                    break;

                case ACTIVITY_ACTIVATED:
                    workflowInstanceActivityStarted(event);
                    break;

                case ACTIVITY_READY:
                case ACTIVITY_COMPLETING:
                    workflowInstanceUpdated(event);
                    break;

                case ACTIVITY_COMPLETED:
                case ACTIVITY_TERMINATED:
                case GATEWAY_ACTIVATED:
                case START_EVENT_OCCURRED:
                case END_EVENT_OCCURRED:
                    workflowInstanceActivityEnded(event);
                    break;

                case SEQUENCE_FLOW_TAKEN:
                    sequenceFlowTaken(event);
                    break;

                case PAYLOAD_UPDATED:
                    workflowInstancePayloadUpdated(event);
                    break;

                default:
                    break;
            }

            insertRecord(event);
        }

        @Override
        public void onIncidentEvent(IncidentEvent event) throws Exception
        {
            switch (event.getState())
            {
                case CREATED:
                    workflowInstanceIncidentOccured(event);
                    break;

                case RESOLVE_FAILED:
                    workflowInstanceIncidentUpdated(event);
                    break;

                case RESOLVED:
                case DELETED:
                    workflowInstanceIncidentResolved(event);
                    break;

                default:
                    break;
            }

            insertRecord(event);
        }

    }

    private void insertRecord(Record record)
    {
        final RecordMetadata metadata = record.getMetadata();

        loggedEventRepository.save(new RecordEntity(metadata.getPartitionId(), metadata.getPosition(), record.toJson()));
    }

    private void workflowInstanceStarted(WorkflowInstanceEntity instance)
    {
        workflowInstanceRepository.save(instance);
    }

    private void workflowInstanceEnded(WorkflowInstanceEvent event)
    {
        final WorkflowInstanceEntity instance = workflowInstanceRepository.findByWorkflowInstanceKeyAndPartitionId(event.getWorkflowInstanceKey(), event.getMetadata().getPartitionId());
        instance.setLastWorkflowInstanceEventPosition(event.getMetadata().getPosition());

        instance.setEnded(true);

        workflowInstanceRepository.save(instance);
    }

    private void workflowInstanceActivityStarted(WorkflowInstanceEvent event)
    {
        final WorkflowInstanceEntity instance = workflowInstanceRepository.findByWorkflowInstanceKeyAndPartitionId(event.getWorkflowInstanceKey(), event.getMetadata().getPartitionId());

        instance.activityStarted(event.getActivityId(), event.getPayload())
                .setLastEventPosition(event.getMetadata().getPosition());

        workflowInstanceRepository.save(instance);
    }

    private void workflowInstanceActivityEnded(WorkflowInstanceEvent event)
    {
        final WorkflowInstanceEntity instance = workflowInstanceRepository.findByWorkflowInstanceKeyAndPartitionId(event.getWorkflowInstanceKey(), event.getMetadata().getPartitionId());

        instance.activityEnded(event.getActivityId(), event.getPayload())
                .setLastEventPosition(event.getMetadata().getPosition());

        workflowInstanceRepository.save(instance);
    }

    private void workflowInstanceCanceled(WorkflowInstanceEvent event)
    {
        final WorkflowInstanceEntity instance = workflowInstanceRepository.findByWorkflowInstanceKeyAndPartitionId(event.getWorkflowInstanceKey(), event.getMetadata().getPartitionId());
        instance.setLastWorkflowInstanceEventPosition(event.getMetadata().getPosition());

        instance.setEnded(true);

        workflowInstanceRepository.save(instance);
    }

    private void workflowInstancePayloadUpdated(WorkflowInstanceEvent event)
    {
        final WorkflowInstanceEntity instance = workflowInstanceRepository.findByWorkflowInstanceKeyAndPartitionId(event.getWorkflowInstanceKey(), event.getMetadata().getPartitionId());

        instance.setPayload(event.getPayload());

        workflowInstanceRepository.save(instance);
    }

    private void workflowInstanceUpdated(WorkflowInstanceEvent event)
    {
        final WorkflowInstanceEntity instance = workflowInstanceRepository.findByWorkflowInstanceKeyAndPartitionId(event.getWorkflowInstanceKey(), event.getMetadata().getPartitionId());

        instance.setPayload(event.getPayload())
                .setLastEventPosition(event.getMetadata().getPosition());

        workflowInstanceRepository.save(instance);
    }

    private void workflowInstanceIncidentOccured(IncidentEvent event)
    {
        final RecordMetadata metadata = event.getMetadata();
        final IncidentEntity incident = new IncidentEntity(metadata.getKey(), event.getWorkflowInstanceKey(), event.getActivityId(), event.getErrorType(), event.getErrorMessage());

        final WorkflowInstanceEntity instance = workflowInstanceRepository.findByWorkflowInstanceKeyAndPartitionId(event.getWorkflowInstanceKey(), metadata.getPartitionId());

        instance.incidentOccured(incident);

        workflowInstanceRepository.save(instance);
    }

    private void workflowInstanceIncidentUpdated(IncidentEvent event)
    {
        final RecordMetadata metadata = event.getMetadata();

        final WorkflowInstanceEntity workflowInstance = workflowInstanceRepository.findByWorkflowInstanceKeyAndPartitionId(event.getWorkflowInstanceKey(), metadata.getPartitionId());

        workflowInstance
            .getIncidents()
            .stream()
            .filter(i -> i.getIncidentKey() == event.getKey())
            .findAny()
            .ifPresent(incident ->
            {
                incident
                    .setErrorType(event.getErrorType())
                    .setErrorMessage(event.getErrorMessage());

                workflowInstanceRepository.save(workflowInstance);
            });
    }

    private void workflowInstanceIncidentResolved(IncidentEvent event)
    {
        final RecordMetadata metadata = event.getMetadata();

        final WorkflowInstanceEntity workflowInstance = workflowInstanceRepository.findByWorkflowInstanceKeyAndPartitionId(event.getWorkflowInstanceKey(), metadata.getPartitionId());

        workflowInstance.getIncidents().removeIf(i -> i.getIncidentKey() == event.getKey());

        workflowInstanceRepository.save(workflowInstance);
    }

    private void sequenceFlowTaken(WorkflowInstanceEvent event)
    {
        final WorkflowInstanceEntity instance = workflowInstanceRepository.findByWorkflowInstanceKeyAndPartitionId(event.getWorkflowInstanceKey(), event.getMetadata().getPartitionId());

        instance.sequenceFlowTaken(event.getActivityId());

        workflowInstanceRepository.save(instance);
    }

}
