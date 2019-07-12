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

@Entity(name = "MESSAGE_SUBSCRIPTION")
public class MessageSubscriptionEntity {

  @Id
  @Column(name = "ID_")
  private String id;

  @Column(name = "MESSAGE_NAME_")
  private String messageName;

  @Column(name = "CORRELATION_KEY_")
  private String correlationKey;

  @Column(name = "WORKFLOW_INSTANCE_KEY_")
  private Long workflowInstanceKey;

  @Column(name = "ELEMENT_INSTANCE_KEY_")
  private Long elementInstanceKey;

    @Column(name = "WORKFLOW_KEY_")
    private Long workflowKey;

    @Column(name = "TARGET_FLOW_NODE_ID_")
    private String targetFlowNodeId;

  @Column(name = "STATE_")
  private String state;

  @Column(name = "TIMESTAMP_")
  private long timestamp;

  public String getCorrelationKey() {
    return correlationKey;
  }

  public void setCorrelationKey(String correlationKey) {
    this.correlationKey = correlationKey;
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

  public String getMessageName() {
    return messageName;
  }

  public void setMessageName(String messageName) {
    this.messageName = messageName;
  }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getWorkflowKey() {
        return workflowKey;
    }

    public void setWorkflowKey(Long workflowKey) {
        this.workflowKey = workflowKey;
    }

    public String getTargetFlowNodeId() {
        return targetFlowNodeId;
    }

    public void setTargetFlowNodeId(String targetFlowNodeId) {
        this.targetFlowNodeId = targetFlowNodeId;
    }

    public Long getWorkflowInstanceKey() {
        return workflowInstanceKey;
    }

    public void setWorkflowInstanceKey(Long workflowInstanceKey) {
        this.workflowInstanceKey = workflowInstanceKey;
    }

    public Long getElementInstanceKey() {
        return elementInstanceKey;
    }

    public void setElementInstanceKey(Long elementInstanceKey) {
        this.elementInstanceKey = elementInstanceKey;
    }
}
