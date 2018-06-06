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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.zeebe.client.api.clients.WorkflowClient;
import io.zeebe.client.api.commands.Workflow;
import io.zeebe.client.api.events.DeploymentEvent;
import io.zeebe.zeebemonitor.entity.WorkflowEntity;
import io.zeebe.zeebemonitor.repository.WorkflowInstanceRepository;
import io.zeebe.zeebemonitor.repository.WorkflowRepository;
import io.zeebe.zeebemonitor.zeebe.WorkflowService;
import io.zeebe.zeebemonitor.zeebe.ZeebeConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/workflows")
public class WorkflowResource
{
    @Autowired
    private ZeebeConnectionService connections;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Autowired
    private WorkflowService workflowService;

    @RequestMapping("/")
    public List<WorkflowDto> getWorkflows()
    {
        final List<WorkflowDto> dtos = new ArrayList<>();
        for (WorkflowEntity workflowEntity : workflowRepository.findAll())
        {
            final WorkflowDto dto = toDto(workflowEntity);
            dtos.add(dto);
        }

        return dtos;
    }

    @RequestMapping(path = "/{workflowKey}")
    public WorkflowDto findWorkflow(@PathVariable("workflowKey") long workflowKey)
    {
        return workflowRepository
                .findById(workflowKey)
                .map(this::toDto)
                .orElse(null);
    }

    private WorkflowDto toDto(WorkflowEntity workflowEntity)
    {
        final long workflowKey = workflowEntity.getWorkflowKey();

        final long countRunning = workflowInstanceRepository.countRunningInstances(workflowKey);
        final long countEnded = workflowInstanceRepository.countEndedInstances(workflowKey);

        final WorkflowDto dto = WorkflowDto.from(workflowEntity, countRunning, countEnded);
        return dto;
    }

    @RequestMapping(path = "/{workflowKey}", method = RequestMethod.POST)
    public void createWorkflowInstance(@PathVariable("workflowKey") long workflowKey, @RequestBody String payload)
    {
        final WorkflowEntity workflow = workflowRepository
                .findById(workflowKey)
                .orElseThrow(() -> new RuntimeException("no workflow found with key: " + workflowKey));

        connections
            .getClient()
            .topicClient(workflow.getTopic())
            .workflowClient()
            .newCreateInstanceCommand()
            .workflowKey(workflowKey)
            .payload(payload)
            .send()
            .join();
    }

    @RequestMapping(path = "/", method = RequestMethod.POST)
    public void uploadModel(@RequestBody DeploymentDto deployment) throws UnsupportedEncodingException
    {
        final String deploymentTopic = deployment.getTopic();

        final WorkflowClient workflowClient = connections.getClient()
                .topicClient(deploymentTopic)
                .workflowClient();

        final List<Long> workflowKeys = new ArrayList<>();

        for (FileDto file : deployment.getFiles())
        {
            final DeploymentEvent deploymentEvent = workflowClient
                .newDeployCommand()
                .addResourceBytes(file.getContent(), file.getFilename())
                .send()
                .join();

            final List<Long> keys = deploymentEvent
                    .getDeployedWorkflows()
                    .stream()
                    .map(Workflow::getWorkflowKey)
                    .collect(Collectors.toList());

            workflowKeys.addAll(keys);
        }

        workflowService.loadWorkflowsByKey(deploymentTopic, workflowKeys);
    }

}
