package io.zeebe.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@EnableWebMvc
public class StaticConfig implements WebMvcConfigurer {

  Logger logger = LoggerFactory.getLogger(StaticConfig.class);
  @Value("${server.context-path}")
  private String base_path;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    logger.info(String.format("Setting %s as application base path.", base_path));
    registry.addResourceHandler(String.format("%s**", base_path))
      .addResourceLocations(
        "classpath:/META-INF/resources/",
        "classpath:/resources/",
        "classpath:/static/",
        "classpath:/public/");
    registry.addResourceHandler(String.format("%swebjars/**", base_path))
      .addResourceLocations("/webjars/", "classpath:/META-INF/resources/webjars/")
      .resourceChain(false);
  }
}