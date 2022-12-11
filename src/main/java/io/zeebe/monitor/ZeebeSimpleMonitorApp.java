/*
 * Copyright ┬® 2017 camunda services GmbH (info@camunda.com)
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
@EnableZeebeClient
@EnableScheduling
@EnableAsync
@EnableSpringDataWebSupport
public class ZeebeSimpleMonitorApp {
  @Value("${server.allowedOriginsUrls}")
  private String allowedOriginsUrls;

  private static final Logger LOG = LoggerFactory.getLogger(ZeebeSimpleMonitorApp.class);
  public static final String REPLACEMENT_CHARACTER_QUESTIONMARK = "\u2370"; // == ⍰ character
  public static final String IMPLEMENTATION_VERSION = "Implementation-Version";

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
    return Mustache.compiler()
        .defaultValue(REPLACEMENT_CHARACTER_QUESTIONMARK)
        .withLoader(templateLoader);
  }

  @Bean
  public Attributes loadAttributesFromManifest() {
    final ClassLoader classLoader = ZeebeSimpleMonitorApp.class.getClassLoader();
    if (classLoader instanceof URLClassLoader) {
      URLClassLoader cl = (URLClassLoader) classLoader;
      URL url = cl.findResource("META-INF/MANIFEST.MF");
      try (InputStream is = url.openStream()) {
        Manifest manifest = new Manifest(is);
        return manifest.getMainAttributes();
      } catch (IOException e) {
        LOG.warn("can't determine version info from manifest, error: " + e.getMessage());
      }
    }
    final Attributes attributes = new Attributes();
    attributes.putValue(IMPLEMENTATION_VERSION, "dev");
    return attributes;
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    final String urls = this.allowedOriginsUrls;
    return new WebMvcConfigurerAdapter() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        if (StringUtils.hasText(urls)) {
          String[] allowedOriginsUrlArr = urls.split(";");
          registry.addMapping("/**").allowedOrigins(allowedOriginsUrlArr);
        }
      }
    };
  }

}
