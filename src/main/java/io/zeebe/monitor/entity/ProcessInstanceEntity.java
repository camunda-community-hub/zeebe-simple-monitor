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

@Entity(name = "PROCESS_INSTANCE")
public class ProcessInstanceEntity {

  @Id
  @Column(name = "KEY_")
  private long key;

  @Column(name = "PARTITION_ID_")
  private int partitionId;

  @Column(name = "PROCESS_DEFINITION_KEY_")
  private long processDefinitionKey;

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

  @Column(name = "PARENT_PROCESS_INSTANCE_KEY_")
  private Long parentProcessInstanceKey;

  @Column(name = "PARENT_ELEMENT_INSTANCE_KEY_")
  private Long parentElementInstanceKey;

  public enum State {
    COMPLETED("Completed"),
    ACTIVE("Active"),
    TERMINATED("Terminated");

    private final String title;

    State(String title) {
      this.title = title;
    }

    public String getTitle() {
      return title;
    }
  }

  public long getKey() {
    return key;
  }

  public void setKey(final long key) {
    this.key = key;
  }

  public long getTimestamp() {
    return start;
  }

  public void setTimestamp(final long timestamp) {
    this.start = timestamp;
  }

  public int getPartitionId() {
    return partitionId;
  }

  public void setPartitionId(final int partitionId) {
    this.partitionId = partitionId;
  }

  public long getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(final long processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getBpmnProcessId() {
    return bpmnProcessId;
  }

  public void setBpmnProcessId(final String bpmnProcessId) {
    this.bpmnProcessId = bpmnProcessId;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(final int version) {
    this.version = version;
  }

  public long getStart() {
    return start;
  }

  public void setStart(final long start) {
    this.start = start;
  }

  public Long getEnd() {
    return end;
  }

  public void setEnd(final Long end) {
    this.end = end;
  }

  public String getState() {
    return state;
  }

  public void setState(final String state) {
    this.state = state;
  }

  public Long getParentProcessInstanceKey() {
    return parentProcessInstanceKey;
  }

  public void setParentProcessInstanceKey(final Long parentprocessInstanceKey) {
    this.parentProcessInstanceKey = parentprocessInstanceKey;
  }

  public Long getParentElementInstanceKey() {
    return parentElementInstanceKey;
  }

  public void setParentElementInstanceKey(final Long parentElementInstanceKey) {
    this.parentElementInstanceKey = parentElementInstanceKey;
  }
}
