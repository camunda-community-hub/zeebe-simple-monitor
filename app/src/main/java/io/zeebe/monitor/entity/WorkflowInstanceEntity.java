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
package io.zeebe.monitor.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "WORKFLOW_INSTANCE")
public class WorkflowInstanceEntity {

  @Id
  @Column(name = "KEY_")
  private long key;

  @Column(name = "PARTITION_ID_")
  private int partitionId;

  @Column(name = "WORKFLOW_KEY_")
  private long workflowKey;

  @Column(name = "BPMN_PROCESS_ID_")
  private String bpmnProcessId;

  @Column(name = "VERSION_")
  private int version;

  @Column(name = "STATE_")
  private String state;

  @Column(name = "START_")
  private long start;

  @Column(name = "END_")
  private Long end;

  @Column(name = "PARENT_WORKFLOW_INSTANCE_KEY_")
  private Long parentWorkflowInstanceKey;

  @Column(name = "PARENT_ELEMENT_INSTANCE_KEY_")
  private Long parentElementInstanceKey;

  public long getKey() {
    return key;
  }

  public void setKey(long key) {
    this.key = key;
  }

  public long getTimestamp() {
    return start;
  }

  public void setTimestamp(long timestamp) {
    this.start = timestamp;
  }

  public int getPartitionId() {
    return partitionId;
  }

  public void setPartitionId(int partitionId) {
    this.partitionId = partitionId;
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

  public long getStart() {
    return start;
  }

  public void setStart(long start) {
    this.start = start;
  }

  public Long getEnd() {
    return end;
  }

  public void setEnd(Long end) {
    this.end = end;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public Long getParentWorkflowInstanceKey() {
    return parentWorkflowInstanceKey;
  }

  public void setParentWorkflowInstanceKey(Long parentWorkflowInstanceKey) {
    this.parentWorkflowInstanceKey = parentWorkflowInstanceKey;
  }

  public Long getParentElementInstanceKey() {
    return parentElementInstanceKey;
  }

  public void setParentElementInstanceKey(Long parentElementInstanceKey) {
    this.parentElementInstanceKey = parentElementInstanceKey;
  }
}
