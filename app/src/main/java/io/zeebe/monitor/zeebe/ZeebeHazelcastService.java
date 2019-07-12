package io.zeebe.monitor.zeebe;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import io.zeebe.hazelcast.connect.java.ZeebeHazelcast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ZeebeHazelcastService {

    private static final Logger LOG = LoggerFactory.getLogger(ZeebeConnectionService.class);

    @Autowired
    private ZeebeImportService importService;

    private HazelcastInstance hazelcast;

    public void start(String hazelcastConnection) {
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress(hazelcastConnection);

        LOG.info("Connecting to Hazelcast '{}'", hazelcastConnection);

        hazelcast = HazelcastClient.newHazelcastClient(clientConfig);

        final ZeebeHazelcast zeebeHazelcast = new ZeebeHazelcast(hazelcast);

        LOG.info("Importing records from Hazelcast...");
        importService.importFrom(zeebeHazelcast);
    }

    public void close() {
        if (hazelcast != null) {
            hazelcast.shutdown();
        }
    }
}
