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

import java.util.*;
import java.util.stream.Collectors;

import io.zeebe.client.api.commands.Workflow;
import io.zeebe.client.api.commands.WorkflowResource;
import io.zeebe.zeebemonitor.entity.WorkflowEntity;
import io.zeebe.zeebemonitor.repository.PartitionRepository;
import io.zeebe.zeebemonitor.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class WorkflowService
{
    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private PartitionRepository partitionRepository;

    @Autowired
    private ZeebeConnectionService connectionService;

    @Async
    public void loadWorkflowByKey(String topic, long workflowKey)
    {
        loadWorkflowsByKey(topic, Collections.singletonList(workflowKey));
    }

    @Async
    public void loadWorkflowsByKey(String topic, final List<Long> workflowKeys)
    {
        workflowKeys.forEach(workflowKey ->
        {
            final WorkflowResource resource = connectionService
                    .getClient()
                    .topicClient(topic)
                    .workflowClient()
                    .newResourceRequest()
                    .workflowKey(workflowKey)
                    .send()
                    .join();

            final WorkflowEntity entity = WorkflowEntity.from(resource, topic);
            workflowRepository.save(entity);
        });
    }

    public void synchronizeWithBroker()
    {
        final List<String> topics = partitionRepository.getTopicNames();

        topics.forEach(this::synchronizeWithBroker);
    }

    private void synchronizeWithBroker(String topic)
    {
        final List<Workflow> workflows = connectionService
            .getClient()
            .topicClient(topic)
            .workflowClient()
            .newWorkflowRequest()
            .send()
            .join()
            .getWorkflows();

        final List<Long> workflowKeys = workflows
                .stream()
                .map(Workflow::getWorkflowKey)
                .collect(Collectors.toList());

        final List<Long> availableWorkflows = new ArrayList<>();
        for (WorkflowEntity workflowEntity : workflowRepository.findAll())
        {
            availableWorkflows.add(workflowEntity.getWorkflowKey());
        }

        workflowKeys.removeAll(availableWorkflows);

        if (!workflowKeys.isEmpty())
        {
            loadWorkflowsByKey(topic, workflowKeys);
        }
    }

}
