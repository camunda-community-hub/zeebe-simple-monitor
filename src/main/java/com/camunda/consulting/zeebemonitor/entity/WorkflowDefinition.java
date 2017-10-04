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
package com.camunda.consulting.zeebemonitor.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import io.zeebe.client.event.WorkflowEvent;

@Entity
public class WorkflowDefinition {

  /**
   * Generate random uuid for unique identification of workflow definition
   */
  @Id
  private String uuid = UUID.randomUUID().toString();

  private String key;
  private int version;

  @OneToOne
  private Broker broker;

  @Column(length = 100000)
  private String resource;

  @Transient
  private long countRunning;
  
  @Transient
  private long countEnded;

  public static WorkflowDefinition from(WorkflowEvent event) {
    WorkflowDefinition dto = new WorkflowDefinition();

    dto.setVersion(event.getVersion());
    dto.setKey(event.getBpmnProcessId());
    dto.setResource(event.getBpmnXml());
    
    return dto;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public Broker getBroker() {
    return broker;
  }

  public void setBroker(Broker broker) {
    this.broker = broker;
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

  @Override
  public String toString() {
    return "WorkflowDefinitionDto [key=" + key + ", broker=" + broker + ", version=" + version + ", countRunning=" + countRunning + ", countEnded=" + countEnded
        + "]";
  }

  public String getUuid() {
    return uuid;
  }

}
