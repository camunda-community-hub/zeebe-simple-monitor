package com.camunda.consulting.zeebemonitor.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Incident {
  
  @Id
  @GeneratedValue
  private long id;

  private String activityId;
  private String errorType;
  private String errorMessage;
  
  public Incident() {    
  }

  public Incident(String activityId, String errorType, String errorMessage) {
    this.activityId = activityId;
    this.errorType = errorType;
    this.errorMessage = errorMessage;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getErrorType() {
    return errorType;
  }

  public void setErrorType(String errorType) {
    this.errorType = errorType;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

}
