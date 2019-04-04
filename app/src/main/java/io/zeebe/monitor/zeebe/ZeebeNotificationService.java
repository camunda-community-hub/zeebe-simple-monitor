package io.zeebe.monitor.zeebe;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import io.zeebe.exporter.proto.Schema.WorkflowInstanceRecord;
import io.zeebe.hazelcast.connect.java.WorkflowInstanceEventListener;
import io.zeebe.monitor.rest.WorkflowInstanceNotification;
import io.zeebe.monitor.rest.WorkflowInstanceNotification.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ZeebeNotificationService {

  @Autowired private SimpMessagingTemplate webSocket;

  @Value("${io.zeebe.monitor.hazelcast.connection}")
  private String hazelcastConnection;

  @Value("${io.zeebe.monitor.hazelcast.topic}")
  private String hazelcastTopic;

  private HazelcastInstance hazelcast;

  public void start() {
    final ClientConfig clientConfig = new ClientConfig();
    clientConfig.getNetworkConfig().addAddress(hazelcastConnection);

    hazelcast = HazelcastClient.newHazelcastClient(clientConfig);

    final ITopic<byte[]> topic = hazelcast.getTopic(hazelcastTopic);
    topic.addMessageListener(
        new WorkflowInstanceEventListener(this::sendWorkflowInstanceNotification));
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
