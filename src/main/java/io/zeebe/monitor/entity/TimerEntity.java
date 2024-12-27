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

import jakarta.persistence.*;

@Entity(name = "TIMER")
@Table(
    indexes = {
      // performance reason, because we use it in the
      // {@link io.zeebe.monitor.repository.TimerRepository#findByProcessInstanceKey(long)}
      @Index(name = "timer_processInstanceKeyIndex", columnList = "PROCESS_INSTANCE_KEY_"),
    })
public class TimerEntity {

  @Id
  @Column(name = "KEY_")
  private long key;

  @Column(name = "PROCESS_DEFINITION_KEY_")
  private long processDefinitionKey;

  @Column(name = "PROCESS_INSTANCE_KEY_")
  private Long processInstanceKey;

  @Column(name = "ELEMENT_INSTANCE_KEY_")
  private Long elementInstanceKey;

  @Column(name = "TARGET_ELEMENT_ID_")
  private String targetElementId;

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

  public void setKey(final long key) {
    this.key = key;
  }

  public Long getElementInstanceKey() {
    return elementInstanceKey;
  }

  public void setElementInstanceKey(final Long elementInstanceKey) {
    this.elementInstanceKey = elementInstanceKey;
  }

  public String getState() {
    return state;
  }

  public void setState(final String state) {
    this.state = state;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(final long timestamp) {
    this.timestamp = timestamp;
  }

  public String getTargetElementId() {
    return targetElementId;
  }

  public void setTargetElementId(final String targetFlowNodeId) {
    this.targetElementId = targetFlowNodeId;
  }

  public long getDueDate() {
    return dueDate;
  }

  public void setDueDate(final long dueDate) {
    this.dueDate = dueDate;
  }

  public long getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(final long processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public Long getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(final Long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public int getRepetitions() {
    return repetitions;
  }

  public void setRepetitions(final int repetitions) {
    this.repetitions = repetitions;
  }
}
