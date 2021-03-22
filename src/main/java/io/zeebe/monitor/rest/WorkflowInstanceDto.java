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
  private int version;

  private String state;
  private boolean isRunning;

  private String startTime = "";
  private String endTime = "";

  private Long parentWorkflowInstanceKey;
  private String parentBpmnProcessId = "";

  private List<VariableEntry> variables = new ArrayList<>();
  private List<ActiveScope> activeScopes = new ArrayList<>();

  private List<ElementInstanceState> elementInstances = new ArrayList<>();

  private List<AuditLogEntry> auditLogEntries = new ArrayList<>();

  private List<String> activeActivities = new ArrayList<>();
  private List<String> incidentActivities = new ArrayList<>();
  private List<String> takenSequenceFlows = new ArrayList<>();

  private List<IncidentDto> incidents = new ArrayList<>();
  private List<JobDto> jobs = new ArrayList<>();
  private List<MessageSubscriptionDto> messageSubscriptions = new ArrayList<>();
  private List<TimerDto> timers = new ArrayList<>();
  private List<CalledWorkflowInstanceDto> calledWorkflowInstances = new ArrayList<>();
  private List<ErrorDto> errors = new ArrayList<>();

  private List<BpmnElementInfo> bpmnElementInfos = new ArrayList<>();

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

  public long getWorkflowKey() {
    return workflowKey;
  }

  public void setWorkflowKey(long workflowKey) {
    this.workflowKey = workflowKey;
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

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public List<ElementInstanceState> getElementInstances() {
    return elementInstances;
  }

  public void setElementInstances(List<ElementInstanceState> elementInstances) {
    this.elementInstances = elementInstances;
  }

  public List<AuditLogEntry> getAuditLogEntries() {
    return auditLogEntries;
  }

  public void setAuditLogEntries(List<AuditLogEntry> auditLogEntries) {
    this.auditLogEntries = auditLogEntries;
  }

  public boolean isRunning() {
    return isRunning;
  }

  public void setRunning(boolean isRunning) {
    this.isRunning = isRunning;
  }

  public List<String> getActiveActivities() {
    return activeActivities;
  }

  public void setActiveActivities(List<String> activeActivities) {
    this.activeActivities = activeActivities;
  }

  public List<String> getIncidentActivities() {
    return incidentActivities;
  }

  public void setIncidentActivities(List<String> incidentActivities) {
    this.incidentActivities = incidentActivities;
  }

  public List<JobDto> getJobs() {
    return jobs;
  }

  public void setJobs(List<JobDto> jobs) {
    this.jobs = jobs;
  }

  public List<MessageSubscriptionDto> getMessageSubscriptions() {
    return messageSubscriptions;
  }

  public void setMessageSubscriptions(List<MessageSubscriptionDto> messageSubscriptions) {
    this.messageSubscriptions = messageSubscriptions;
  }

  public List<TimerDto> getTimers() {
    return timers;
  }

  public void setTimers(List<TimerDto> timers) {
    this.timers = timers;
  }

  public List<VariableEntry> getVariables() {
    return variables;
  }

  public void setVariables(List<VariableEntry> variables) {
    this.variables = variables;
  }

  public List<ActiveScope> getActiveScopes() {
    return activeScopes;
  }

  public void setActiveScopes(List<ActiveScope> activeScopes) {
    this.activeScopes = activeScopes;
  }

  public Long getParentWorkflowInstanceKey() {
    return parentWorkflowInstanceKey;
  }

  public void setParentWorkflowInstanceKey(Long parentWorkflowInstanceKey) {
    this.parentWorkflowInstanceKey = parentWorkflowInstanceKey;
  }

  public String getParentBpmnProcessId() {
    return parentBpmnProcessId;
  }

  public void setParentBpmnProcessId(String parentBpmnProcessId) {
    this.parentBpmnProcessId = parentBpmnProcessId;
  }

  public boolean hasParentWorkflowInstance() {
    return parentWorkflowInstanceKey != null && parentWorkflowInstanceKey > 0;
  }

  public List<CalledWorkflowInstanceDto> getCalledWorkflowInstances() {
    return calledWorkflowInstances;
  }

  public void setCalledWorkflowInstances(List<CalledWorkflowInstanceDto> calledWorkflowInstances) {
    this.calledWorkflowInstances = calledWorkflowInstances;
  }

  public List<BpmnElementInfo> getBpmnElementInfos() {
    return bpmnElementInfos;
  }

  public void setBpmnElementInfos(List<BpmnElementInfo> bpmnElementInfos) {
    this.bpmnElementInfos = bpmnElementInfos;
  }

  public List<ErrorDto> getErrors() {
    return errors;
  }

  public void setErrors(List<ErrorDto> errors) {
    this.errors = errors;
  }
}
