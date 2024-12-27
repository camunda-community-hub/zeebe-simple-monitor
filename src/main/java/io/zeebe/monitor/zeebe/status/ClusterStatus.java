package io.zeebe.monitor.zeebe.status;

import static java.lang.String.valueOf;

import io.camunda.zeebe.client.api.response.PartitionBrokerHealth;
import io.camunda.zeebe.client.api.response.PartitionInfo;
import io.camunda.zeebe.client.api.response.Topology;
import java.util.Collection;

public class ClusterStatus {

  private Topology topology;
  private String healthyString = valueOf(PartitionBrokerHealth.UNHEALTHY).toLowerCase();
  private boolean healthy = false;

  public void setTopology(Topology topology) {
    this.topology = topology;
    boolean unHealthy = true;
    if (topology != null) {
      unHealthy =
          topology.getBrokers().stream()
              .map(
                  brokerInfo ->
                      brokerInfo.getPartitions().stream().map(PartitionInfo::getHealth).toList())
              .flatMap(Collection::stream)
              .anyMatch(health -> health != PartitionBrokerHealth.HEALTHY);
    }
    healthy = topology != null && !unHealthy;
    healthyString =
        valueOf(healthy ? PartitionBrokerHealth.HEALTHY : PartitionBrokerHealth.UNHEALTHY)
            .toLowerCase();
  }

  public String getHealthyString() {
    return healthyString;
  }

  public boolean isHealthy() {
    return healthy;
  }

  public Topology getTopology() {
    return topology;
  }
}
