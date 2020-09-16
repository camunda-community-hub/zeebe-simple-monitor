package io.zeebe.monitor.zeebe.hazelcast;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zeebe.exporter.proto.Schema;
import io.zeebe.exporter.proto.Schema.DeploymentRecord;
import io.zeebe.hazelcast.connect.java.ZeebeHazelcast;
import io.zeebe.monitor.entity.HazelcastConfig;
import io.zeebe.monitor.repository.HazelcastConfigRepository;
import io.zeebe.monitor.zeebe.ZeebeImportService;
import io.zeebe.monitor.zeebe.protobuf.ProtobufSource;

@Configuration
@ConditionalOnProperty(name="zeebe.client.worker.hazelcast.enabled", havingValue="true")
@EnableConfigurationProperties(value={HazelcastProperties.class})
public class HazelcastConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(HazelcastConfiguration.class);

  @Autowired HazelcastProperties hazelcastProperties;

  @Bean
  public HazelcastInstance hazelcastInstance(HazelcastProperties hazelcastProperties) {
    
    final ClientConfig clientConfig = new ClientConfig();
    clientConfig.getNetworkConfig().addAddress(hazelcastProperties.getConnection());

    final var connectionRetryConfig =
        clientConfig.getConnectionStrategyConfig().getConnectionRetryConfig();
        
    connectionRetryConfig.setClusterConnectTimeoutMillis(
        Duration.parse(hazelcastProperties.getConnectionTimeout()).toMillis());

    LOG.info("Connecting to Hazelcast '{}'", hazelcastProperties.getConnection());

    return HazelcastClient.newHazelcastClient(clientConfig);

  }

  @Bean
  public ZeebeHazelcast zeebeHazelcastBuilder(
    HazelcastInstance hazelcastInstance,
    HazelcastConfigRepository hazelcastConfigRepository,
    ZeebeImportService zeebeProtobufImportService) {

    final var hazelcastConfig =
        hazelcastConfigRepository
            .findById("cfg")
            .orElseGet(
                () -> {
                  final var config = new HazelcastConfig();
                  config.setId("cfg");
                  config.setSequence(-1);
                  return config;
                });

    final var builder = ZeebeHazelcast.newBuilder(hazelcastInstance);

    // TODO consider moving ProtobufSource into zeebe-exporter-protobuf and get ZeebeHazelcast.Builder to implement it
    zeebeProtobufImportService.importFrom(new ProtobufSource(){
      public void addDeploymentListener(Consumer<Schema.DeploymentRecord> listener) {
        builder.addDeploymentListener(listener);
      }
      public void addWorkflowInstanceListener(Consumer<Schema.WorkflowInstanceRecord> listener) {
        builder.addWorkflowInstanceListener(listener);
      }
      public void addVariableListener(Consumer<Schema.VariableRecord> listener) {
        builder.addVariableListener(listener);
      }
      public void addVariableDocumentListener(Consumer<Schema.VariableDocumentRecord> listener) {
        builder.addVariableDocumentListener(listener);
      }
      public void addJobListener(Consumer<Schema.JobRecord> listener) {
        builder.addJobListener(listener);
      }
      public void addJobBatchListener(Consumer<Schema.JobBatchRecord> listener) {
        builder.addJobBatchListener(listener);
      }
      public void addIncidentListener(Consumer<Schema.IncidentRecord> listener) {
        builder.addIncidentListener(listener);
      }
      public void addTimerListener(Consumer<Schema.TimerRecord> listener) {
        builder.addTimerListener(listener);
      }
      public void addMessageListener(Consumer<Schema.MessageRecord> listener) {
        builder.addMessageListener(listener);
      }
      public void addMessageSubscriptionListener(Consumer<Schema.MessageSubscriptionRecord> listener) {
        builder.addMessageSubscriptionListener(listener);
      }
      public void addMessageStartEventSubscriptionListener(Consumer<Schema.MessageStartEventSubscriptionRecord> listener) {
        builder.addMessageStartEventSubscriptionListener(listener);
      }
      public void addWorkflowInstanceSubscriptionListener(Consumer<Schema.WorkflowInstanceSubscriptionRecord> listener) {
        builder.addWorkflowInstanceSubscriptionListener(listener);
      }
      public void addWorkflowInstanceCreationListener(Consumer<Schema.WorkflowInstanceCreationRecord> listener) {
        builder.addWorkflowInstanceCreationListener(listener);
      }
      public void addWorkflowInstanceResultListener(Consumer<Schema.WorkflowInstanceResultRecord> listener) {
        builder.addWorkflowInstanceResultListener(listener);
      }
      public void addErrorListener(Consumer<Schema.ErrorRecord> listener) {
        builder.addErrorListener(listener);
      }
    
    });
            
    builder.postProcessListener(
      sequence -> {
        hazelcastConfig.setSequence(sequence);
        hazelcastConfigRepository.save(hazelcastConfig);
      });

    if (hazelcastConfig.getSequence() >= 0) {
      builder.readFrom(hazelcastConfig.getSequence());
    } else {
      builder.readFromHead();
    }

    return builder.build();
  }

}