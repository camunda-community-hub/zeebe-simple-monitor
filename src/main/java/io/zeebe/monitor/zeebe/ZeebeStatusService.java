package io.zeebe.monitor.zeebe;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.Topology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ZeebeStatusService {

  @Autowired private ZeebeClient zeebeClient;

  public Topology getTopology() {
    return zeebeClient.newTopologyRequest()
        .requestTimeout(Duration.ofSeconds(5))
        .send()
        .join();
  }


}
