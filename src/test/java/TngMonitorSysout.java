import java.util.Properties;

import org.camunda.tngp.client.TngpClient;

import com.camunda.consulting.tngp.listener.TngpEventPolling;

public class TngMonitorSysout {

  public static void main(String[] args) throws InterruptedException {
    TngpClient client = TngpClient.create(new Properties());
    client.connect();

    TngpEventPolling eventPolling = new TngpEventPolling();

    eventPolling.pollAllTopics(client);

    client.disconnect();
    client.close();
    // System.out.println(WorkflowDefinitionResource.definitions);
  }
}
