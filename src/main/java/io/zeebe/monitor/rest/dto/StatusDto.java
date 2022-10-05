package io.zeebe.monitor.rest.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class StatusDto {
  private String gatewayVersion;
  private String simpleMonitorVersion;
  private int clusterSize;
  private int replicationFactor;
  private int partitionsCount;
  private List<BrokerDto> brokers = new ArrayList<>();

  public void setClusterSize(int clusterSize) {
    this.clusterSize = clusterSize;
  }

  public int getClusterSize() {
    return clusterSize;
  }

  public void setGatewayVersion(String gatewayVersion) {
    this.gatewayVersion = gatewayVersion;
  }

  public String getGatewayVersion() {
    return gatewayVersion;
  }

  public void setReplicationFactor(int replicationFactor) {
    this.replicationFactor = replicationFactor;
  }

  public int getReplicationFactor() {
    return replicationFactor;
  }

  public void setPartitionsCount(int partitionsCount) {
    this.partitionsCount = partitionsCount;
  }

  public int getPartitionsCount() {
    return partitionsCount;
  }

  public void addBroker(BrokerDto brokerDto) {
    this.brokers.add(brokerDto);
  }

  public List<BrokerDto> getBrokers() {
    return brokers;
  }

  public void setSimpleMonitorVersion(String simpleMonitorVersion) {
    this.simpleMonitorVersion = simpleMonitorVersion;
  }

  public String getSimpleMonitorVersion() {
    return simpleMonitorVersion;
  }

}
