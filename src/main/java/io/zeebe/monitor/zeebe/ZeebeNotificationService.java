package io.zeebe.monitor.zeebe;

import io.zeebe.monitor.rest.WorkflowInstanceNotification;
import io.zeebe.monitor.rest.WorkflowInstanceNotification.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ZeebeNotificationService {

  private static final Logger LOG = LoggerFactory.getLogger(ZeebeNotificationService.class);

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
    webSocket.convertAndSend("/notifications/workflow-instance", notification);
  }
}
