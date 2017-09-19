package com.camunda.consulting.zeebemonitor.entity;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class LoggedEvent {
  @Id
  @GeneratedValue
  private Long id;

  @OneToOne
  private Broker broker;

  @Column(length = 20000)
  private String eventPayload;

  private int partitionId;

  private long position;

  private long key;

  private String eventType;

  private String state;

  public LoggedEvent() {}

  public LoggedEvent(Broker broker, int partitionId, long position, long key, String eventType, String state, String eventPayload) {
    this.broker = broker;
    this.partitionId = partitionId;
    this.position = position;
    this.key = key;
    this.eventType = eventType;
    this.state = state;

    if (eventPayload!=null && eventPayload.length()>=20000) {
      eventPayload = eventPayload.substring(0, 19999);
    }

    this.eventPayload = eventPayload;
  }

  public String getPayload() {
    return eventPayload;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Broker getBroker() {
    return broker;
  }

  public void setBroker(Broker broker) {
    this.broker = broker;
  }

  public String getEventPayload() {
    return eventPayload;
  }

  public int getPartitionId() {
    return partitionId;
  }

  public long getPosition() {
    return position;
  }

  public long getKey() {
    return key;
  }

  public String getEventType() {
    return eventType;
  }

  public String getState() {
    return state;
  }



}
