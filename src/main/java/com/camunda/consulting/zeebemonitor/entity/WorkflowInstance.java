package com.camunda.consulting.zeebemonitor.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import io.zeebe.client.event.WorkflowInstanceEvent;

@Entity
public class WorkflowInstance {

  @Id
  private long id;

  @OneToOne
  private Broker broker;
  private String workflowDefinitionUuid;

  private String workflowDefinitionKey;
  private int workflowDefinitionVersion;

  private boolean ended = false;

  @Column(length = 20000)
  private String payload;

  @LazyCollection(LazyCollectionOption.FALSE)
  @ElementCollection
  private List<String> runningActivities = new ArrayList<>();

  @LazyCollection(LazyCollectionOption.FALSE)
  @ElementCollection
  private List<String> endedActivities = new ArrayList<>();

  @LazyCollection(LazyCollectionOption.FALSE)
  @OneToMany(cascade=CascadeType.ALL)
  private List<Incident> incidents = new ArrayList<>();

  public static WorkflowInstance from(WorkflowInstanceEvent workflowInstanceEvent) {
    WorkflowInstance dto = new WorkflowInstance();

    dto.setWorkflowDefinitionKey(workflowInstanceEvent.getBpmnProcessId());
    dto.setWorkflowDefinitionVersion(workflowInstanceEvent.getVersion());
    dto.setId(workflowInstanceEvent.getWorkflowInstanceKey());

    dto.setPayload(workflowInstanceEvent.getPayload());

    return dto;
  }

  public WorkflowInstance activityStarted(String activityId, String newPayload) {
    // TODO: Add own activity entity to also allow for loops & co
    runningActivities.add(activityId);
    setPayload(newPayload);
    return this;
  }

  public WorkflowInstance activityEnded(String activityId, String newPayload) {
    runningActivities.remove(activityId);
    endedActivities.add(activityId);
    setPayload(newPayload);
    return this;
  }

  public WorkflowInstance incidentOccured(Incident incident) {
    this.incidents.add(incident);
    return this;
  }

  public Broker getBroker() {
    return broker;
  }

  public void setBroker(Broker broker) {
    this.broker = broker;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    if (payload!=null && payload.length()>0) {
      this.payload = payload;
    }
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
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

  public WorkflowInstance setEnded(boolean ended) {
    this.ended = ended;
    return this;
  }

  public String getWorkflowDefinitionUuid() {
    return workflowDefinitionUuid;
  }

  public void setWorkflowDefinitionUuid(String workflowDefinitionUuid) {
    this.workflowDefinitionUuid = workflowDefinitionUuid;
  }

  @Override
  public String toString() {
    return "WorkflowInstanceDto [broker=" + broker + ", id=" + id + ", workflowDefinitionUuid=" + workflowDefinitionUuid + ", ended=" + ended + ", payload="
        + payload + ", runningActivities=" + runningActivities + ", endedActivities=" + endedActivities + "]";
  }

  public String getWorkflowDefinitionKey() {
    return workflowDefinitionKey;
  }

  public void setWorkflowDefinitionKey(String workflowDefinitionKey) {
    this.workflowDefinitionKey = workflowDefinitionKey;
  }

  public int getWorkflowDefinitionVersion() {
    return workflowDefinitionVersion;
  }

  public void setWorkflowDefinitionVersion(int workflowDefinitionVersion) {
    this.workflowDefinitionVersion = workflowDefinitionVersion;
  }

  public List<Incident> getIncidents() {
    return this.incidents;
  }

}
