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

import io.zeebe.client.api.events.WorkflowInstanceEvent;
import io.zeebe.client.api.record.ZeebeObjectMapper;
import io.zeebe.zeebemonitor.entity.RecordEntity;
import io.zeebe.zeebemonitor.entity.WorkflowInstanceEntity;
import io.zeebe.zeebemonitor.repository.RecordRepository;
import io.zeebe.zeebemonitor.repository.WorkflowInstanceRepository;
import io.zeebe.zeebemonitor.zeebe.ZeebeConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instances")
public class WorkflowInstanceResource
{

    @Autowired
    private ZeebeConnectionService connections;

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Autowired
    private RecordRepository recordRepository;

    @RequestMapping("/")
    public Iterable<WorkflowInstanceEntity> getWorkflowInstances()
    {
        return workflowInstanceRepository.findAll();
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
    public void cancelWorkflowInstance(@PathVariable("id") String id) throws Exception
    {
        final WorkflowInstanceEntity workflowInstance = workflowInstanceRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("no workflow instance found with id: " + id));

        final WorkflowInstanceEvent event = findWorkflowInstanceEvent(workflowInstance.getPartitionId(), workflowInstance.getLastWorkflowInstanceEventPosition());

        connections
            .getClient()
            .topicClient(workflowInstance.getTopicName())
            .workflowClient()
            .newCancelInstanceCommand(event)
            .send()
            .join();
    }

    @RequestMapping(path = "/{id}/update-payload", method = RequestMethod.PUT)
    public void updatePayload(@PathVariable("id") String id, @RequestBody String payload) throws Exception
    {

        final WorkflowInstanceEntity workflowInstance = workflowInstanceRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("no workflow instance found with id: " + id));

        final WorkflowInstanceEvent event = findWorkflowInstanceEvent(workflowInstance.getPartitionId(), workflowInstance.getLastEventPosition());

        connections
            .getClient()
            .topicClient(workflowInstance.getTopicName())
            .workflowClient()
            .newUpdatePayloadCommand(event)
            .payload(payload)
            .send()
            .join();
    }

    private WorkflowInstanceEvent findWorkflowInstanceEvent(int partitionId, long position) throws Exception
    {
        final RecordEntity record = recordRepository.findByPartitionIdAndPosition(partitionId, position);

        if (record == null)
        {
            throw new RuntimeException(String.format("no record found at partition '%d' and position '%d'", partitionId, position));
        }

        final ZeebeObjectMapper objectMapper = connections.getClient().objectMapper();

        return objectMapper.fromJson(record.getContentAsJson(), WorkflowInstanceEvent.class);
    }

}
