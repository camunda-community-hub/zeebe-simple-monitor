package io.zeebe.monitor.rest.dto;

public class PartitionInfoDto {

  private int partitionId;
  private String role;
  private String health;
  private boolean leader;

  public void setPartitionId(int partitionId) {
    this.partitionId = partitionId;
  }

  public int getPartitionId() {
    return partitionId;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getRole() {
    return role;
  }

  public void setHealth(String health) {
    this.health = health;
  }

  public String getHealth() {
    return health;
  }

  public void setLeader(boolean leader) {
    this.leader = leader;
  }

  public boolean getLeader() {
    return leader;
  }
}
