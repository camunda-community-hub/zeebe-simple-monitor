package io.zeebe.monitor.zeebe.status;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.Topology;
import io.zeebe.monitor.rest.ui.ClusterHealthyNotification;
import io.zeebe.monitor.zeebe.ZeebeNotificationService;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ZeebeStatusUpdateService {

  private static final Logger LOG = LoggerFactory.getLogger(ZeebeStatusUpdateService.class);

  @Autowired private ZeebeClient zeebeClient;
  @Autowired private ZeebeStatusKeeper zeebeStatusKeeper;
  @Autowired private ZeebeNotificationService zeebeNotificationService;

  @Scheduled(fixedRate = 3000)
  public void scheduleFixedRateWithInitialDelayTask() {
    final var status = new ClusterStatus();
    try {
      status.setTopology(getTopologyFromCluster());
    } catch (Exception e) {
      // Stacking the exceptions, to make them better readable, like this:
      // Can't get status from cluster, errors (stacked): io exception;
      // io.grpc.StatusRuntimeException: UNAVAILABLE: io exception; UNAVAILABLE: io exception;
      // Connection refused: /127.0.0.1:26500; Connection refused;
      StringBuilder sb = new StringBuilder();
      for (Throwable t = e; t != null; t = t.getCause()) {
        sb.append(t.getMessage()).append("; ");
      }
      LOG.warn("Can't get status from cluster, errors (stacked): " + sb);
    }
    zeebeStatusKeeper.setStatus(status);
    zeebeNotificationService.sendClusterStatusUpdate(
        new ClusterHealthyNotification(status.getHealthyString(), status.isHealthy()));
  }

  private Topology getTopologyFromCluster() {
    return zeebeClient.newTopologyRequest().requestTimeout(Duration.ofSeconds(2)).send().join();
  }
}
