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

import java.util.ArrayList;
import java.util.List;

public class WorkflowInstanceDto {
  private int partitionId;

  private long workflowInstanceKey;

  private String bpmnProcessId;
  private long workflowKey;
  private int workflowVersion;

  private boolean ended = false;

  private String payload;

  private List<String> runningActivities = new ArrayList<>();
  private List<String> endedActivities = new ArrayList<>();
  private List<String> takenSequenceFlows = new ArrayList<>();

  private List<IncidentDto> incidents = new ArrayList<>();

  private String workflowResource;

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public int getPartitionId() {
    return partitionId;
  }

  public void setPartitionId(int partitionId) {
    this.partitionId = partitionId;
  }

  public long getWorkflowInstanceKey() {
    return workflowInstanceKey;
  }

  public void setWorkflowInstanceKey(long workflowInstanceKey) {
    this.workflowInstanceKey = workflowInstanceKey;
  }

  public String getBpmnProcessId() {
    return bpmnProcessId;
  }

  public void setBpmnProcessId(String bpmnProcessId) {
    this.bpmnProcessId = bpmnProcessId;
  }

  public List<String> getRunningActivities() {
    return runningActivities;
  }

  public void setRunningActivities(List<String> runningActivities) {
    this.runningActivities = runningActivities;
  }

  public List<String> getEndedActivities() {
    return endedActivities;
  }

  public void setEndedActivities(List<String> endedActivities) {
    this.endedActivities = endedActivities;
  }

  public boolean isEnded() {
    return ended;
  }

  public WorkflowInstanceDto setEnded(boolean ended) {
    this.ended = ended;
    return this;
  }

  public long getWorkflowKey() {
    return workflowKey;
  }

  public void setWorkflowKey(long workflowKey) {
    this.workflowKey = workflowKey;
  }

  public int getWorkflowVersion() {
    return workflowVersion;
  }

  public void setWorkflowVersion(int workflowVersion) {
    this.workflowVersion = workflowVersion;
  }

  public List<String> getTakenSequenceFlows() {
    return takenSequenceFlows;
  }

  public void setTakenSequenceFlows(List<String> takenSequenceFlows) {
    this.takenSequenceFlows = takenSequenceFlows;
  }

  public List<IncidentDto> getIncidents() {
    return incidents;
  }

  public void setIncidents(List<IncidentDto> incidents) {
    this.incidents = incidents;
  }

  public String getWorkflowResource() {
    return workflowResource;
  }

  public void setWorkflowResource(String workflowResource) {
    this.workflowResource = workflowResource;
  }
}
