package com.camunda.consulting.zeebemonitor.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Incident {

  @Id
  private long id;

  private String activityId;
  private String errorType;
  private String errorMessage;

  public Incident() {
  }

  public Incident(long id, String activityId, String errorType, String errorMessage) {
    this.id = id;
    this.activityId = activityId;
    this.errorType = errorType;
    this.errorMessage = errorMessage;
  }

  public long getId()
  {
      return id;
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

  public Incident setErrorType(String errorType) {
    this.errorType = errorType;
    return this;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public Incident setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
        return true;
    }
    if (obj == null)
    {
        return false;
    }
    if (getClass() != obj.getClass())
    {
        return false;
    }
    Incident other = (Incident) obj;
    if (id != other.id)
    {
        return false;
    }
    return true;
  }

}
