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

@Entity(name = "ELEMENT_INSTANCE")
@Table(indexes = {
    @Index(name = "element_instance_processInstanceKeyIndex", columnList = "PROCESS_INSTANCE_KEY_"),
    @Index(name = "element_instance_processDefinitionKeyIndex", columnList = "PROCESS_DEFINITION_KEY_"),
    @Index(name = "element_instance_intentIndex", columnList = "INTENT_"),
    @Index(name = "element_instance_bpmnElementTypeIndex", columnList = "BPMN_ELEMENT_TYPE_"),
})
public class ElementInstanceEntity {

  @Id
  @Column(name = "ID")
  private String id;

  @Column(name = "POSITION_")
  private Long position;

  @Column(name = "PARTITION_ID_")
  private int partitionId;

  @Column(name = "KEY_")
  private long key;

  @Column(name = "INTENT_")
  private String intent;

  @Column(name = "PROCESS_INSTANCE_KEY_")
  private long processInstanceKey;

  @Column(name = "ELEMENT_ID_")
  private String elementId;

  @Column(name = "BPMN_ELEMENT_TYPE_")
  private String bpmnElementType;

  @Column(name = "FLOW_SCOPE_KEY_")
  private long flowScopeKey;

  @Column(name = "PROCESS_DEFINITION_KEY_")
  private long processDefinitionKey;

  @Column(name = "TIMESTAMP_")
  private long timestamp;

  public String getId() {
    return id;
  }

  private void setId(final String id) {
    // made private, to avoid accidental changes
    this.id = id;
  }

  public final String getGeneratedIdentifier() {
    return this.partitionId + "-" + this.position;
  }

  @PrePersist
  private void prePersistDeriveIdField() {
    setId(getGeneratedIdentifier());
  }

  public long getKey() {
    return key;
  }

  public void setKey(final long key) {
    this.key = key;
  }

  public String getIntent() {
    return intent;
  }

  public void setIntent(final String intent) {
    this.intent = intent;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(final long timestamp) {
    this.timestamp = timestamp;
  }

  public String getElementId() {
    return elementId;
  }

  public void setElementId(final String elementId) {
    this.elementId = elementId;
  }

  public long getFlowScopeKey() {
    return flowScopeKey;
  }

  public void setFlowScopeKey(final long flowScopeKey) {
    this.flowScopeKey = flowScopeKey;
  }

  public int getPartitionId() {
    return partitionId;
  }

  public void setPartitionId(final int partitionId) {
    this.partitionId = partitionId;
  }

  public long getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(final long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public long getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(final long processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public Long getPosition() {
    return position;
  }

  public void setPosition(final Long position) {
    this.position = position;
  }

  public String getBpmnElementType() {
    return bpmnElementType;
  }

  public void setBpmnElementType(final String bpmnElementType) {
    this.bpmnElementType = bpmnElementType;
  }
}
