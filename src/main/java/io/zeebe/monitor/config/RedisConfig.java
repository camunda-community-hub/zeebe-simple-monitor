package io.zeebe.monitor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = "zeebe-importer", havingValue = "redis")
@Configuration
public class RedisConfig {

  @Value("${zeebe.client.worker.redis.connection}")
  private String redisConnection;

  @Value("${zeebe.client.worker.redis.useClusterClient:false}")
  private boolean useClusterClient;

  @Value("${zeebe.client.worker.redis.consumer-group:simple-monitor}")
  private String redisConumerGroup;

  @Value("${zeebe.client.worker.redis.xread-count:500}")
  private int redisXreadCount;

  @Value("${zeebe.client.worker.redis.xread-block-millis:2000}")
  private int redisXreadBlockMillis;

  @Value("${zeebe.client.worker.redis.prefix:zeebe}")
  private String redisPrefix;

  public String getRedisConnection() {
    return redisConnection;
  }

  public boolean isUseClusterClient() {
    return useClusterClient;
  }

  public String getRedisConumerGroup() {
    return redisConumerGroup;
  }

  public int getRedisXreadCount() {
    return redisXreadCount;
  }

  public int getRedisXreadBlockMillis() {
    return redisXreadBlockMillis;
  }

  public String getRedisPrefix() {
    return redisPrefix;
  }
}
