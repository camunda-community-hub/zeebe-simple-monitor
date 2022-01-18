package io.zeebe.monitor.zeebe;

import io.zeebe.monitor.rest.ui.ProcessInstanceNotification;
import io.zeebe.monitor.rest.ui.ProcessInstanceNotification.Type;
import io.zeebe.monitor.rest.ui.ZeebeClusterNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ZeebeNotificationService {

  private final String basePath;

  public ZeebeNotificationService(@Value("${server.servlet.context-path}") final String basePath) {
    this.basePath = basePath.endsWith("/") ? basePath : basePath + "/";
  }

  @Autowired private SimpMessagingTemplate webSocket;

  public void sendProcessInstanceUpdated(
      final long processInstanceKey, final long processDefinitionKey) {
    final ProcessInstanceNotification notification = new ProcessInstanceNotification();
    notification.setProcessInstanceKey(processInstanceKey);
    notification.setProcessDefinitionKey(processDefinitionKey);
    notification.setType(Type.UPDATED);

    sendNotification(notification);
  }

  public void sendCreatedProcessInstance(
      final long processInstanceKey, final long processDefinitionKey) {
    final ProcessInstanceNotification notification = new ProcessInstanceNotification();
    notification.setProcessInstanceKey(processInstanceKey);
    notification.setProcessDefinitionKey(processDefinitionKey);
    notification.setType(Type.CREATED);

    sendNotification(notification);
  }

  public void sendEndedProcessInstance(
      final long processInstanceKey, final long processDefinitionKey) {
    final ProcessInstanceNotification notification = new ProcessInstanceNotification();
    notification.setProcessInstanceKey(processInstanceKey);
    notification.setProcessDefinitionKey(processDefinitionKey);
    notification.setType(Type.REMOVED);

    sendNotification(notification);
  }

  public void sendZeebeClusterError(final String message) {
    final ZeebeClusterNotification notification = new ZeebeClusterNotification();
    notification.setMessage(message);
    notification.setType(ZeebeClusterNotification.Type.ERROR);
    sendNotification(notification);
  }

  private void sendNotification(final ProcessInstanceNotification notification) {
    final var destination = basePath + "notifications/process-instance";
    webSocket.convertAndSend(destination, notification);
  }

  private void sendNotification(final ZeebeClusterNotification notification) {
    final var destination = basePath + "notifications/zeebe-cluster";
    webSocket.convertAndSend(destination, notification);
  }
}
