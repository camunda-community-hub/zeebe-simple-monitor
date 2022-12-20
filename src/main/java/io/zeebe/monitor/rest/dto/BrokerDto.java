package io.zeebe.monitor.rest.dto;

import java.util.ArrayList;
import java.util.List;

public class BrokerDto {
  private int nodeId;
  private String address;
  private String host;
  private int port;
  private String version;
  private List<PartitionInfoDto> partitionInfos = new ArrayList<>();

  public void setAddress(String address) {
    this.address = address;
  }

  public String getAddress() {
    return address;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getHost() {
    return host;
  }

  public void setNodeId(int nodeId) {
    this.nodeId = nodeId;
  }

  public int getNodeId() {
    return nodeId;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int getPort() {
    return port;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getVersion() {
    return version;
  }

  public void addPartitionInfo(PartitionInfoDto partitionInfoDto) {
    this.partitionInfos.add(partitionInfoDto);
  }

  public List<PartitionInfoDto> getPartitionInfos() {
    return partitionInfos;
  }
}
