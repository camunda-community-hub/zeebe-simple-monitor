package io.zeebe.monitor.config;

import com.sun.istack.NotNull;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaConfig {
    @Value("${kafkaBootstrapServers:localhost:9092}")
    public String kafkaServers;
    @Value("${kafkaSchemaRegistryUrl:localhost:8081}")
    public String schemaRegistry;
    @Value("${kafka.streams.applicationId:zeebe-simple-monitor}")
    public String applicationId;
    @Value("${kafka.streams.numOfThreads:4}")
    public int numOfThreads;
    @Value("${kafka.streams.maxMessageSizeBytes:5242880}")
    private int maxMessageSizeBytes;

    public static final double MAX_PARTITION_FETCH_BYTES_CONFIG_MULTIPLIER = 1.1;
    public static final String TOPIC_VNOC_API_REQUEST = "vnoc-api-request";
    public static final String TOPIC_VNOC_API_RESPONSE = "vnoc-api-response";

    public String getKafkaServers() {
        return kafkaServers;
    }

    public String getSchemaRegistry() {
        return schemaRegistry;
    }

    public int getMaxMessageSizeBytes() {
        return this.maxMessageSizeBytes;
    }

    public void setMaxMessageSizeBytes(int var1) {
        this.maxMessageSizeBytes = var1;
    }

    @Bean({"schemaRegistry"})
    public String getSchemaRegistryUrl() {
        return this.schemaRegistry;
    }

    public String getApplicationId() {
        return this.applicationId;
    }
    public int getNumOfThreads() {
        return numOfThreads;
    }


    public void setKafkaServers(@NotNull String var1) {
        this.kafkaServers = var1;
    }


    public void setSchemaRegistry(@NotNull String var1) {
        this.schemaRegistry = var1;
    }



    private Map getSecurityProps() {
        Map<String, Object> props = new HashMap<>();
        String securityProtocol = System.getenv("KAFKA_SECURITY_PROTOCOL");
        if (securityProtocol == null) {
            securityProtocol = "PLAINTEXT";
        }
        props.put("security.protocol", securityProtocol);

        String truststorePassword = System.getenv("KAFKA_TRUSTSTORE_PASSWORD");;
        if (truststorePassword != null) {
            props.put("ssl.truststore.location", "/tmp/kafka-certs/ca.p12");
            props.put("ssl.truststore.type", "PKCS12");
            props.put("ssl.truststore.password", truststorePassword);
        }

        String keystorePassword = System.getenv("KAFKA_KEYSTORE_PASSWORD");
        if (keystorePassword != null) {
            props.put("ssl.keystore.location", "/tmp/user-certs/user.p12");
            props.put("ssl.keystore.type", "PKCS12");
            props.put("ssl.keystore.password", keystorePassword);
        }

        return props;
    }

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration streamsConfig() {
        System.out.println("bbot"+getKafkaServers());
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, this.getApplicationId());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, this.getKafkaServers());

        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put("num.stream.threads", this.getNumOfThreads());
        props.put("max.request.size", this.getMaxMessageSizeBytes());
        props.put("max.partition.fetch.bytes", (int)((double)this.getMaxMessageSizeBytes() * 1.1));
        props.putAll(this.getSecurityProps());
        return new KafkaStreamsConfiguration(props);
    }
}
