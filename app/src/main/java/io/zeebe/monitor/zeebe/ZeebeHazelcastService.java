package io.zeebe.monitor.zeebe;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ZeebeHazelcastService {

  private static final Logger LOG = LoggerFactory.getLogger(ZeebeConnectionService.class);

  @Autowired private ZeebeImportService importService;

  private AutoCloseable closeable;

  public void start(String hazelcastConnection) {
    final ClientConfig clientConfig = new ClientConfig();
    clientConfig.getNetworkConfig().addAddress(hazelcastConnection);

    LOG.info("Connecting to Hazelcast '{}'", hazelcastConnection);

    final HazelcastInstance hazelcast = HazelcastClient.newHazelcastClient(clientConfig);

    LOG.info("Importing records from Hazelcast...");
    closeable = importService.importFrom(hazelcast);
  }

  public void close() {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
