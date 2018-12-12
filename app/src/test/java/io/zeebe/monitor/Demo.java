package io.zeebe.monitor;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.clients.JobClient;
import io.zeebe.client.api.clients.WorkflowClient;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

public class Demo {

  public static void main(String[] args) throws InterruptedException {

    final ZeebeClient client = ZeebeClient.newClient();
    final WorkflowClient workflowClient = client.workflowClient();
    final JobClient jobClient = client.jobClient();

    workflowClient
        .newDeployCommand()
        .addResourceFromClasspath("orderProcess.bpmn")
        .addResourceFromClasspath("ship-parcel.bpmn")
        .addResourceFromClasspath("payment.bpmn")
        .send()
        .join();

    workflowClient
        .newCreateInstanceCommand()
        .bpmnProcessId("order-process")
        .latestVersion()
        .send()
        .join();

    workflowClient
        .newCreateInstanceCommand()
        .bpmnProcessId("ship-parcel")
        .latestVersion()
        .send()
        .join();

    workflowClient
        .newCreateInstanceCommand()
        .bpmnProcessId("payment")
        .latestVersion()
        .send()
        .join();

    jobClient
        .newWorker()
        .jobType("collect-money")
        .handler(
            (c, job) ->
                c.newCompleteCommand(job.getKey())
                    .payload(Collections.singletonMap("totalPrice", 49.95))
                    .send()
                    .join())
        .open();

    jobClient
        .newWorker()
        .jobType("fetch-items")
        .handler((c, job) -> c.newCompleteCommand(job.getKey()).send().join())
        .open();

    jobClient
        .newWorker()
        .jobType("ship-parcel")
        .handler(
            (c, job) -> c.newFailCommand(job.getKey()).retries(job.getRetries() - 1).send().join())
        .open();

    jobClient
        .newWorker()
        .jobType("payment")
        .handler(
            (c, job) ->
                c.newCompleteCommand(job.getKey())
                    .payload(Collections.singletonMap("success", false))
                    .send()
                    .join())
        .open();

    // wait until killed
    new CountDownLatch(1).await();
  }
}
