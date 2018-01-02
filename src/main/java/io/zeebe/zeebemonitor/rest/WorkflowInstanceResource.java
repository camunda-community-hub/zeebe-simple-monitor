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
package io.zeebe.zeebemonitor.rest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.clustering.impl.TopicLeader;
import io.zeebe.client.event.TopicSubscription;
import io.zeebe.client.event.WorkflowInstanceEvent;
import io.zeebe.zeebemonitor.Constants;
import io.zeebe.zeebemonitor.entity.WorkflowInstance;
import io.zeebe.zeebemonitor.repository.WorkflowInstanceRepository;
import io.zeebe.zeebemonitor.zeebe.ZeebeConnections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow-instance")
public class WorkflowInstanceResource
{

    @Autowired
    private ZeebeConnections connections;

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;

    @RequestMapping("/")
    public Iterable<WorkflowInstance> getWorkflowInstances()
    {
        return workflowInstanceRepository.findAll();
    }

    @RequestMapping(path = "/{key}", method = RequestMethod.DELETE)
    public void cancelWorkflowInstance(@PathVariable("key") long workflowInstanceKey) throws Exception
    {

        final WorkflowInstance workflowInstance = workflowInstanceRepository.findOne(workflowInstanceKey);
        if (workflowInstance != null)
        {
            final ZeebeClient client = connections.getZeebeClient(workflowInstance.getBroker());

            final WorkflowInstanceEvent event = findLastWorkflowInstanceEvent(workflowInstance, workflowInstanceKey);

            client.workflows() //
                  .cancel(event).execute();
        }
    }

    @RequestMapping(path = "/{key}/update-payload", method = RequestMethod.PUT)
    public void updatePayload(@PathVariable("key") long workflowInstanceKey, @RequestBody String payload) throws Exception
    {

        final WorkflowInstance workflowInstance = workflowInstanceRepository.findOne(workflowInstanceKey);
        if (workflowInstance != null)
        {
            final ZeebeClient client = connections.getZeebeClient(workflowInstance.getBroker());

            final WorkflowInstanceEvent event = findLastWorkflowInstanceEvent(workflowInstance, workflowInstance.getLastEventPosition());

            client.workflows() //
                  .updatePayload(event).payload(payload).execute();
        }
    }

    private WorkflowInstanceEvent findLastWorkflowInstanceEvent(WorkflowInstance workflowInstance, long position) throws Exception
    {
        final CompletableFuture<WorkflowInstanceEvent> future = new CompletableFuture<>();

        final ZeebeClient client = connections.getZeebeClient(workflowInstance.getBroker());

        TopicSubscription subscription = null;
        try
        {
            final int partitionId = getPartitionIdOfTopic(client, Constants.DEFAULT_TOPIC);

            subscription = client.topics()
                                 .newSubscription(Constants.DEFAULT_TOPIC)
                                 .name("wf-instance-lookup")
                                 .forcedStart()
                                 .startAtPosition(partitionId, position - 1)
                                 .workflowInstanceEventHandler(wfEvent -> {
                                     if (wfEvent.getMetadata().getPosition() == position)
                                     {
                                         future.complete(wfEvent);
                                     }
                                 })
                                 .open();

            return future.get(10, TimeUnit.SECONDS);
        }
        finally
        {
            if (subscription != null)
            {
                subscription.close();
            }
        }
    }

    private int getPartitionIdOfTopic(final ZeebeClient client, String topic)
    {
        return client.requestTopology().execute()
                .getTopicLeaders()
                .stream()
                .filter(tl -> tl.getTopicName().equals(topic))
                .findFirst()
                .map(TopicLeader::getPartitionId)
                .orElseThrow(() -> new RuntimeException("Doesn't find topic with name: " + topic));
    }

}
