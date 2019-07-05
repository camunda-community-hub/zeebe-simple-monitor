package io.zeebe.monitor.zeebe;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import io.zeebe.exporter.proto.Schema.WorkflowInstanceRecord;
import io.zeebe.hazelcast.connect.java.ZeebeHazelcast;
import io.zeebe.monitor.rest.WorkflowInstanceNotification;
import io.zeebe.monitor.rest.WorkflowInstanceNotification.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ZeebeNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(ZeebeNotificationService.class);

  @Autowired private SimpMessagingTemplate webSocket;

  @Value("${io.zeebe.monitor.hazelcast.connection}")
  private String hazelcastConnection;

  @Value("${io.zeebe.monitor.hazelcast.topic}")
  private String hazelcastTopic;

  private HazelcastInstance hazelcast;

  public void start() {
    final ClientConfig clientConfig = new ClientConfig();
    clientConfig.getNetworkConfig().addAddress(hazelcastConnection);

      try {
          LOG.info("Connecting to Hazelcast '{}'", hazelcastConnection);

          hazelcast = HazelcastClient.newHazelcastClient(clientConfig);

          LOG.info("Listening on Hazelcast topic '{}' for workflow instances", hazelcastTopic);

          new ZeebeHazelcast(hazelcast)
                  .addWorkflowInstanceListener(hazelcastTopic, this::sendWorkflowInstanceNotification);

      } catch (Exception e) {
          LOG.warn("Failed to connect to Hazelcast. Still works but no updates will be received.", e);
      }
  }

  private void sendWorkflowInstanceNotification(WorkflowInstanceRecord workflowInstance) {

    final WorkflowInstanceNotification notification = new WorkflowInstanceNotification();
    notification.setWorkflowInstanceKey(workflowInstance.getWorkflowInstanceKey());
    notification.setWorkflowKey(workflowInstance.getWorkflowKey());

    notification.setType(Type.UPDATED);

    final String intent = workflowInstance.getMetadata().getIntent();
    if (workflowInstance.getElementId().equals(workflowInstance.getBpmnProcessId())) {
      if (intent.equals("ELEMENT_ACTIVATING")) {
        notification.setType(Type.CREATED);
      } else if (intent.equals("ELEMENT_TERMINATED")) {
        notification.setType(Type.REMOVED);
      }
    }

    webSocket.convertAndSend("/notifications/workflow-instance", notification);
  }

  public void close() {
    hazelcast.shutdown();
  }
}
