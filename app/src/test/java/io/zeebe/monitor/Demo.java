package io.zeebe.monitor;

import io.zeebe.client.ZeebeClient;
import java.util.Collections;

public class Demo {

  public static void main(String[] args) throws InterruptedException {

    final ZeebeClient client = ZeebeClient.newClient();

    client
        .newDeployCommand()
        .addResourceFromClasspath("orderProcess.bpmn")
        .addResourceFromClasspath("ship-parcel.bpmn")
        .addResourceFromClasspath("payment.bpmn")
        .send()
        .join();

    client.newCreateInstanceCommand().bpmnProcessId("order-process").latestVersion().send().join();

    client.newCreateInstanceCommand().bpmnProcessId("ship-parcel").latestVersion().send().join();

    client.newCreateInstanceCommand().bpmnProcessId("payment").latestVersion().send().join();

    client
        .newWorker()
        .jobType("collect-money")
        .handler(
            (c, job) ->
                c.newCompleteCommand(job.getKey())
                    .payload(Collections.singletonMap("totalPrice", 49.95))
                    .send()
                    .join())
        .open();

    client
        .newWorker()
        .jobType("fetch-items")
        .handler((c, job) -> c.newCompleteCommand(job.getKey()).send().join())
        .open();

    client
        .newWorker()
        .jobType("ship-parcel")
        .handler(
            (c, job) ->
                c.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage("Bad luck ;)")
                    .send()
                    .join())
        .open();

    client
        .newWorker()
        .jobType("payment")
        .handler(
            (c, job) ->
                c.newCompleteCommand(job.getKey())
                    .payload(Collections.singletonMap("success", false))
                    .send()
                    .join())
        .open();

    Thread.sleep(5_000);
  }
}
