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

@Entity(name = "TIMER")
public class TimerEntity {

  @Id
  @Column(name = "KEY_")
  private long key;

  @Column(name = "WORKFLOW_KEY_")
  private long workflowKey;

  @Column(name = "WORKFLOW_INSTANCE_KEY_")
  private Long workflowInstanceKey;

  @Column(name = "ELEMENT_INSTANCE_KEY_")
  private Long elementInstanceKey;

  @Column(name = "TARGET_FLOW_NODE_ID_")
  private String targetFlowNodeId;

  @Column(name = "DUE_DATE_")
  private long dueDate;

    @Column(name = "REPETITIONS")
    private int repetitions;

  @Column(name = "STATE_")
  private String state;

  @Column(name = "TIMESTAMP_")
  private long timestamp;

  public long getKey() {
    return key;
  }

  public void setKey(long key) {
    this.key = key;
  }

  public Long getElementInstanceKey() {
    return elementInstanceKey;
  }

  public void setElementInstanceKey(Long elementInstanceKey) {
    this.elementInstanceKey = elementInstanceKey;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getTargetFlowNodeId() {
    return targetFlowNodeId;
  }

  public void setTargetFlowNodeId(String targetFlowNodeId) {
    this.targetFlowNodeId = targetFlowNodeId;
  }

  public long getDueDate() {
    return dueDate;
  }

  public void setDueDate(long dueDate) {
    this.dueDate = dueDate;
  }

  public long getWorkflowKey() {
    return workflowKey;
  }

  public void setWorkflowKey(long workflowKey) {
    this.workflowKey = workflowKey;
  }

  public Long getWorkflowInstanceKey() {
    return workflowInstanceKey;
  }

  public void setWorkflowInstanceKey(Long workflowInstanceKey) {
    this.workflowInstanceKey = workflowInstanceKey;
  }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }
}
