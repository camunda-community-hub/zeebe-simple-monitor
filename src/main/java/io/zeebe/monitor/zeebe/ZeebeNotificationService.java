package io.zeebe.monitor.zeebe;

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

  private final String base_path;

  public ZeebeNotificationService(@Value("${server.servlet.context-path}") final String base_path) {
    this.base_path = base_path.endsWith("/") ? base_path : base_path + "/";
  }

  @Autowired private SimpMessagingTemplate webSocket;

  public void sendWorkflowInstanceUpdated(long workflowInstanceKey, long workflowKey) {
    final WorkflowInstanceNotification notification = new WorkflowInstanceNotification();
    notification.setWorkflowInstanceKey(workflowInstanceKey);
    notification.setWorkflowKey(workflowKey);
    notification.setType(Type.UPDATED);

    sendNotification(notification);
  }

  public void sendCreatedWorkflowInstance(long workflowInstanceKey, long workflowKey) {
    final WorkflowInstanceNotification notification = new WorkflowInstanceNotification();
    notification.setWorkflowInstanceKey(workflowInstanceKey);
    notification.setWorkflowKey(workflowKey);
    notification.setType(Type.CREATED);

    sendNotification(notification);
  }

  public void sendEndedWorkflowInstance(long workflowInstanceKey, long workflowKey) {
    final WorkflowInstanceNotification notification = new WorkflowInstanceNotification();
    notification.setWorkflowInstanceKey(workflowInstanceKey);
    notification.setWorkflowKey(workflowKey);
    notification.setType(Type.REMOVED);

    sendNotification(notification);
  }

  private void sendNotification(final WorkflowInstanceNotification notification) {
    final var destination = base_path +
            "notifications/workflow-instance";
    webSocket.convertAndSend(destination, notification);
  }
}
