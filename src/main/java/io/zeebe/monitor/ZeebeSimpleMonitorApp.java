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

import com.samskivert.mustache.Mustache;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.time.Instant.now;

@SpringBootApplication
@EnableZeebeClient
@EnableScheduling
@EnableAsync
@EnableSpringDataWebSupport
public class ZeebeSimpleMonitorApp {

  public static void main(final String... args) {
    SpringApplication.run(ZeebeSimpleMonitorApp.class, args);
  }

  @Bean
  public ScheduledExecutorService scheduledExecutor() {
    return Executors.newSingleThreadScheduledExecutor();
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

  @Bean
  public Mustache.Compiler configureFallbackValueForMissingVariablesInMustacheTemplates(
      Mustache.TemplateLoader templateLoader) {
    return Mustache.compiler().defaultValue("⍰").withLoader(templateLoader);
  }

  @Bean
  @ConditionalOnMissingBean(BuildProperties.class)
  BuildProperties buildProperties() {
    return new BuildProperties(developmentBuildProperties());
  }

  private Properties developmentBuildProperties() {
    final Properties entries = new Properties();
    entries.setProperty("group", "io.zeebe");
    entries.setProperty("artifact", "zeebe-simple-monitor");
    entries.setProperty("name", "Zeebe Simple Monitor");
    entries.setProperty("version", "development build");
    entries.setProperty("time", now().toString());
    return entries;
  }
}
