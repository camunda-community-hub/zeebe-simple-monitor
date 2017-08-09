package com.camunda.consulting.zeebe.dto;

public class IncidentDto {

  private String activityId;
  private String errorType;
  private String errorMessage;
  
  public IncidentDto() {    
  }

  public IncidentDto(String activityId, String errorType, String errorMessage) {
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
