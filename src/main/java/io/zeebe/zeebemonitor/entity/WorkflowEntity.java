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
package io.zeebe.zeebemonitor.entity;

import io.zeebe.client.api.commands.WorkflowResource;
import org.springframework.data.annotation.Id;

public class WorkflowEntity
{

    @Id
    private long workflowKey;

    private String bpmnProcessId;

    private int version;

    private String topic;

    private String resource;

    public static WorkflowEntity from(WorkflowResource workflowResource, String topic)
    {
        final WorkflowEntity entity = new WorkflowEntity();

        entity.setWorkflowKey(workflowResource.getWorkflowKey());
        entity.setVersion(workflowResource.getVersion());
        entity.setBpmnProcessId(workflowResource.getBpmnProcessId());
        entity.setResource(workflowResource.getBpmnXml());

        entity.setTopic(topic);

        return entity;
    }

    public String getResource()
    {
        return resource;
    }

    public void setResource(String resource)
    {
        this.resource = resource;
    }

    public String getBpmnProcessId()
    {
        return bpmnProcessId;
    }

    public void setBpmnProcessId(String bpmnProcessId)
    {
        this.bpmnProcessId = bpmnProcessId;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public long getWorkflowKey()
    {
        return workflowKey;
    }

    public void setWorkflowKey(long workflowKey)
    {
        this.workflowKey = workflowKey;
    }

    public String getTopic()
    {
        return topic;
    }

    public void setTopic(String topic)
    {
        this.topic = topic;
    }

}
