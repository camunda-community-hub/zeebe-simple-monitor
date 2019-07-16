/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.monitor.zeebe;

import io.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ZeebeConnectionService {
  private static final Logger LOG = LoggerFactory.getLogger(ZeebeConnectionService.class);

  private ZeebeClient client;
  private boolean connected = false;

  public void connect(String connectionString) {
      LOG.info("Connecting to broker '{}'", connectionString);

    this.client = ZeebeClient.newClientBuilder().brokerContactPoint(connectionString).build();

    if (checkConnection()) {
        LOG.info("connected to '{}'", connectionString);

    } else {
        LOG.warn("Failed to connect to '{}'", connectionString);
    }
  }

  public ZeebeClient getClient() {
    if (client != null) {
      return client;
    } else {
      throw new RuntimeException("Monitor is not connected");
    }
  }

  public boolean isConnected() {
    return connected;
  }

  public boolean checkConnection() {
    if (client != null) {
      // send request to check if connected or not
      try {
        client.newTopologyRequest().send().join();

        if (!connected) {
          LOG.info("connected to '{}'", client.getConfiguration().getBrokerContactPoint());
        }

        connected = true;
      } catch (Exception e) {
        connected = false;
      }
    }
    return connected;
  }

  public void disconnect() {
    LOG.info("disconnect");

    client.close();

    connected = false;
    client = null;
  }
}
