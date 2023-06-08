package io.zeebe.monitor.zeebe;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;

@Component
public class ZeebeHazelcastService {

  private static final Logger LOG = LoggerFactory.getLogger(ZeebeHazelcastService.class);

  @Value("${zeebe.client.worker.hazelcast.connection}")
  private String hazelcastConnection;

  @Value("${zeebe.client.worker.hazelcast.connectionTimeout}")
  private String hazelcastConnectionTimeout;

  @Autowired private ZeebeImportService importService;

  private AutoCloseable closeable;

  @PostConstruct
  public void start() {
    final ClientConfig clientConfig = new ClientConfig();
    clientConfig.getNetworkConfig().addAddress(hazelcastConnection);

    final var connectionRetryConfig =
        clientConfig.getConnectionStrategyConfig().getConnectionRetryConfig();
    connectionRetryConfig.setClusterConnectTimeoutMillis(
        Duration.parse(hazelcastConnectionTimeout).toMillis());

    LOG.info("Connecting to Hazelcast '{}'", hazelcastConnection);

    final HazelcastInstance hazelcast = HazelcastClient.newHazelcastClient(clientConfig);

    LOG.info("Importing records from Hazelcast...");
    closeable = importService.importFrom(hazelcast);
  }

  @PreDestroy
  public void close() throws Exception {
    if (closeable != null) {
      closeable.close();
    }
  }
}
