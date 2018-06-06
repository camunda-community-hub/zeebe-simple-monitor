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
package io.zeebe.zeebemonitor;

import java.util.concurrent.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.zeebe.zeebemonitor.repository.ConfigurationRepository;
import io.zeebe.zeebemonitor.zeebe.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class ZeebeSimpleMonitorApp
{
    private static final Logger LOG = LoggerFactory.getLogger(ZeebeSimpleMonitorApp.class);

    @Autowired
    private ZeebeConnectionService connectionService;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private TopicService topicService;

    public static void main(String... args)
    {
        SpringApplication.run(ZeebeSimpleMonitorApp.class, args);
    }

    @PostConstruct
    public void initConnection()
    {
        LOG.info("initialize connection");

        configurationRepository.getConfiguration().ifPresent(config ->
        {
            LOG.debug("configuration found");

            connectionService.connect(config);
        });
    }

    @Scheduled(fixedRate = 10_000)
    public void synchronizeWithBroker()
    {
        if (connectionService.isConnected())
        {
            topicService.synchronizeWithBroker();

            workflowService.synchronizeWithBroker();
        }
    }

    @PreDestroy
    public void close()
    {
        connectionService.disconnect();
    }

    @Bean
    public ScheduledExecutorService scheduledExecutor()
    {
        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        return executor;
    }

    @Bean
    public Executor asyncExecutor()
    {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(32);
        executor.initialize();
        return executor;
    }

}
