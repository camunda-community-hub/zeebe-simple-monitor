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
package com.camunda.consulting.zeebemonitor.repository;

import java.util.List;

import com.camunda.consulting.zeebemonitor.entity.WorkflowInstance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface WorkflowInstanceRepository extends CrudRepository<WorkflowInstance, Long>
{

    List<WorkflowInstance> findByBrokerConnectionString(String brokerId);

    @Query("SELECT COUNT(wf) FROM WorkflowInstance wf WHERE wf.workflowDefinitionKey=?1 and wf.workflowDefinitionVersion=?2 and wf.ended=false")
    long countRunningInstances(String workflowDefinitionKey, int workflowDefinitionVersion);

    @Query("SELECT COUNT(wf) FROM WorkflowInstance wf WHERE wf.workflowDefinitionKey=?1 and wf.workflowDefinitionVersion=?2 and wf.ended=true")
    long countEndedInstances(String workflowDefinitionKey, int workflowDefinitionVersion);

}
