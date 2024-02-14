package io.zeebe.monitor.zeebe.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.zeebe.monitor.config.RedisConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "zeebe-importer", havingValue = "redis")
@Component
public class ZeebeRedisService {

    private static final Logger LOG = LoggerFactory.getLogger(ZeebeRedisService.class);

    private RedisConfig config;

    public ZeebeRedisService(RedisConfig redisConfig) {
        this.config = redisConfig;
    }

    @Autowired
    private RedisImportService importService;

    private AutoCloseable closeable;

    @PostConstruct
    public void start() {
        var redisUri = RedisURI.create(config.getRedisConnection());

        LOG.info("Connecting to Redis {}, consumer group {}", redisUri, config.getRedisConumerGroup());
        if (config.isUseClusterClient()) {
            var redisClient = RedisClusterClient.create(redisUri);
            LOG.info("Importing records from Redis cluster...");
            closeable = importService.importFrom(redisClient, config);
        } else {
            var redisClient = RedisClient.create(redisUri);
            LOG.info("Importing records from Redis...");
            closeable = importService.importFrom(redisClient, config);
        }
    }

    @PreDestroy
    public void close() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }
}
