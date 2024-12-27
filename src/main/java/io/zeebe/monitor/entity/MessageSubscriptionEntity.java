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

@Entity(name = "MESSAGE_SUBSCRIPTION")
@Table(
    indexes = {
      // performance reason, because we use it in the
      // {@link
      // io.zeebe.monitor.repository.MessageSubscriptionRepository#findByProcessInstanceKey(long)}
      @Index(
          name = "message_subscription_processInstanceKeyIndex",
          columnList = "PROCESS_INSTANCE_KEY_"),
    })
public class MessageSubscriptionEntity {

  @Id
  @Column(name = "ID_")
  private String id;

  @Column(name = "MESSAGE_NAME_")
  private String messageName;

  @Column(name = "CORRELATION_KEY_")
  private String correlationKey;

  @Column(name = "PROCESS_INSTANCE_KEY_")
  private Long processInstanceKey;

  @Column(name = "ELEMENT_INSTANCE_KEY_")
  private Long elementInstanceKey;

  @Column(name = "PROCESS_DEFINITION_KEY_")
  private Long processDefinitionKey;

  @Column(name = "TARGET_FLOW_NODE_ID_")
  private String targetFlowNodeId;

  @Column(name = "STATE_")
  private String state;

  @Column(name = "TIMESTAMP_")
  private long timestamp;

  public String getCorrelationKey() {
    return correlationKey;
  }

  public void setCorrelationKey(final String correlationKey) {
    this.correlationKey = correlationKey;
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

  public String getMessageName() {
    return messageName;
  }

  public void setMessageName(final String messageName) {
    this.messageName = messageName;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public Long getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(final Long processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getTargetFlowNodeId() {
    return targetFlowNodeId;
  }

  public void setTargetFlowNodeId(final String targetFlowNodeId) {
    this.targetFlowNodeId = targetFlowNodeId;
  }

  public Long getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(final Long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public Long getElementInstanceKey() {
    return elementInstanceKey;
  }

  public void setElementInstanceKey(final Long elementInstanceKey) {
    this.elementInstanceKey = elementInstanceKey;
  }
}
