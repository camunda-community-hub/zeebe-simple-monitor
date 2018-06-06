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
package io.zeebe.zeebemonitor.zeebe;

import io.zeebe.client.ZeebeClient;
import io.zeebe.zeebemonitor.entity.ConfigurationEntity;
import io.zeebe.zeebemonitor.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ZeebeConnectionService
{
    private static final Logger LOG = LoggerFactory.getLogger(ZeebeConnectionService.class);

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private PartitionRepository partitionRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private ZeebeSubscriber subscriber;

    private ZeebeClient client;
    private boolean connected = false;

    public void connect(final ConfigurationEntity config)
    {
        LOG.info("connect to '{}'", config.getConnectionString());

        this.client = ZeebeClient
                .newClientBuilder()
                .brokerContactPoint(config.getConnectionString())
                .build();

        if (checkConnection())
        {
            LOG.info("connected to {}", config.getConnectionString());

            connected = true;

            partitionRepository
                .getTopicNames()
                .forEach(topic -> subscriber.openSubscription(topic));
        }
        else
        {
            LOG.warn("Failed to connect to {}", config.getConnectionString());
        }
    }

    public ZeebeClient getClient()
    {
        if (client != null)
        {
            return client;
        }
        else
        {
            throw new RuntimeException("Monitor is not connected");
        }
    }

    public boolean isConnected()
    {
        return connected;
    }

    public boolean checkConnection()
    {
        if (client != null)
        {
            // send request to check if connected or not
            try
            {
                client.newTopologyRequest().send().join();

                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public void disconnect()
    {
        LOG.info("disconnect");

        client.close();

        connected = false;
        client = null;
    }

    public void deleteAllData()
    {
        LOG.info("delete all data");

        disconnect();

        workflowInstanceRepository.deleteAll();
        workflowRepository.deleteAll();
        recordRepository.deleteAll();
        configurationRepository.deleteAll();
        partitionRepository.deleteAll();
        subscriptionRepository.deleteAll();
    }

}
