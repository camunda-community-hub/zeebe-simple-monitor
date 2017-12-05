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

import io.zeebe.zeebemonitor.Constants;
import io.zeebe.zeebemonitor.entity.DeploymentDto;
import io.zeebe.zeebemonitor.entity.FileDto;
import io.zeebe.zeebemonitor.entity.WorkflowDefinition;
import io.zeebe.zeebemonitor.repository.WorkflowDefinitionRepository;
import io.zeebe.zeebemonitor.repository.WorkflowInstanceRepository;
import io.zeebe.zeebemonitor.zeebe.ZeebeConnections;
import io.zeebe.client.WorkflowsClient;
import io.zeebe.client.event.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/workflow-definition")
public class WorkflowDefinitionResource
{

    @Autowired
    private ZeebeConnections connections;

    @Autowired
    private WorkflowDefinitionRepository workflowDefinitionRepository;

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;

    @RequestMapping(path = "/")
    public Iterable<WorkflowDefinition> getWorkflowDefinitions()
    {
        return fillWorkflowInstanceCount(workflowDefinitionRepository.findAll());
    }

    private Iterable<WorkflowDefinition> fillWorkflowInstanceCount(Iterable<WorkflowDefinition> workflowDefinitions)
    {
        for (WorkflowDefinition workflowDefinition : workflowDefinitions)
        {
            fillWorkflowInstanceCount(workflowDefinition);
        }
        return workflowDefinitions;
    }

    private WorkflowDefinition fillWorkflowInstanceCount(WorkflowDefinition workflowDefinition)
    {
        workflowDefinition.setCountRunning(workflowInstanceRepository.countRunningInstances(workflowDefinition.getKey(), workflowDefinition.getVersion()));
        workflowDefinition.setCountEnded(workflowInstanceRepository.countEndedInstances(workflowDefinition.getKey(), workflowDefinition.getVersion()));
        return workflowDefinition;
    }

    @RequestMapping(path = "/{broker}/{key}/{version}")
    public WorkflowDefinition findWorkflowDefinition(@PathVariable("broker") String broker, @PathVariable("key") String key,
            @PathVariable("version") int version)
    {
        return fillWorkflowInstanceCount(workflowDefinitionRepository.findByBrokerConnectionStringAndKeyAndVersion(broker, key, version));
    }

    @RequestMapping(path = "/{broker}/{key}/{version}", method = RequestMethod.PUT)
    public void startWorkflowInstance(@PathVariable("broker") String brokerConnection, @PathVariable("key") String key, @PathVariable("version") int version,
            @RequestBody String payload)
    {

        connections.getZeebeClient(brokerConnection).workflows() //
                   .create(Constants.DEFAULT_TOPIC).bpmnProcessId(key).version(version).payload(payload).execute();
    }

    @RequestMapping(path = "/", method = RequestMethod.POST)
    public void uploadModel(@RequestBody DeploymentDto deployment) throws UnsupportedEncodingException
    {
        final WorkflowsClient workflows = connections.getZeebeClient(deployment.getBroker()).workflows();
        for (FileDto file : deployment.getFiles())
        {
            workflows //
                      .deploy(Constants.DEFAULT_TOPIC) //
                      .addResourceBytes(file.getContent(), file.getFilename()).execute();
        }
    }

    private ResourceType getResourceType(FileDto file)
    {
        final String fileName = file.getFilename().toLowerCase();
        if (fileName.endsWith(".yaml") || fileName.endsWith(".yml"))
        {
            return ResourceType.YAML_WORKFLOW;
        }
        else
        {
            return ResourceType.BPMN_XML;
        }
    }

}
