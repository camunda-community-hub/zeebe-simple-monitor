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
package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.WorkflowEntity;

public class WorkflowDto {

  private long workflowKey;
  private String bpmnProcessId;
  private int version;
  private String resource;

  private long countRunning;
  private long countEnded;

  public static WorkflowDto from(WorkflowEntity entity, long countRunning, long countEnded) {
    final WorkflowDto dto = new WorkflowDto();

    dto.workflowKey = entity.getKey();
    dto.bpmnProcessId = entity.getBpmnProcessId();
    dto.version = entity.getVersion();
    dto.resource = entity.getResource();

    dto.countRunning = countRunning;
    dto.countEnded = countEnded;

    return dto;
  }

  public long getWorkflowKey() {
    return workflowKey;
  }

  public void setWorkflowKey(long workflowKey) {
    this.workflowKey = workflowKey;
  }

  public String getBpmnProcessId() {
    return bpmnProcessId;
  }

  public void setBpmnProcessId(String bpmnProcessId) {
    this.bpmnProcessId = bpmnProcessId;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public long getCountRunning() {
    return countRunning;
  }

  public void setCountRunning(long countRunning) {
    this.countRunning = countRunning;
  }

  public long getCountEnded() {
    return countEnded;
  }

  public void setCountEnded(long countEnded) {
    this.countEnded = countEnded;
  }
}
