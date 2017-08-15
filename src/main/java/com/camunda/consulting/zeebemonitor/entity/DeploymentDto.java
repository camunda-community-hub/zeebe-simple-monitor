package com.camunda.consulting.zeebemonitor.entity;

import java.util.List;

public class DeploymentDto {
  
  private String broker;
  private List<FileDto> files;
  
  public String getBroker() {
    return broker;
  }
  public void setBroker(String broker) {
    this.broker = broker;
  }
  public List<FileDto> getFiles() {
    return files;
  }
  public void setFiles(List<FileDto> files) {
    this.files = files;
  }

}
