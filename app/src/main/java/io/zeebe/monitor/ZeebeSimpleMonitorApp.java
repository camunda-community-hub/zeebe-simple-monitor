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
package io.zeebe.monitor;

import io.zeebe.monitor.zeebe.ZeebeConnectionService;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class ZeebeSimpleMonitorApp {
  private static final Logger LOG = LoggerFactory.getLogger(ZeebeSimpleMonitorApp.class);

  @Value("${io.zeebe.monitor.connectionString}")
  private String connectionString;

  @Autowired private ZeebeConnectionService connectionService;

  public static void main(String... args) {
    SpringApplication.run(ZeebeSimpleMonitorApp.class, args);
  }

  @PostConstruct
  public void initConnection() {
    LOG.info("initialize connection");

    connectionService.connect(connectionString);
  }

  @PreDestroy
  public void close() {
    connectionService.disconnect();
  }

  @Bean
  public ScheduledExecutorService scheduledExecutor() {
    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    return executor;
  }

  @Bean
  public Executor asyncExecutor() {
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(1);
    executor.setMaxPoolSize(1);
    executor.setQueueCapacity(32);
    executor.initialize();
    return executor;
  }
}
