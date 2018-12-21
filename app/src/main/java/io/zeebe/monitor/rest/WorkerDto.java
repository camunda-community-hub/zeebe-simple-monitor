package io.zeebe.monitor.rest;

public class WorkerDto {

  private String worker;
  private String jobType;
  private String lastPollRequest;

  public String getJobType() {
    return jobType;
  }

  public void setJobType(String jobType) {
    this.jobType = jobType;
  }

  public String getWorker() {
    return worker;
  }

  public void setWorker(String worker) {
    this.worker = worker;
  }

  public String getLastPollRequest() {
    return lastPollRequest;
  }

  public void setLastPollRequest(String lastPollRequest) {
    this.lastPollRequest = lastPollRequest;
  }
}
