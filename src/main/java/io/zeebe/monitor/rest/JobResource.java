/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.monitor.rest;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.monitor.entity.JobEntity;
import io.zeebe.monitor.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("${server.servlet.context-path}api/jobs")
public class JobResource {

  private static final String WORKER_NAME = "zeebe-simple-monitor";

  @Autowired private ZeebeClient zeebeClient;

  @Autowired private JobRepository jobRepository;

  @RequestMapping(path = "/{key}/complete", method = RequestMethod.PUT)
  public void completeJob(@PathVariable("key") long key, @RequestBody String variables) {

    zeebeClient.newCompleteCommand(key).variables(variables).send().join();
  }

  @RequestMapping(path = "/{key}/fail", method = RequestMethod.PUT)
  public void failJob(@PathVariable("key") long key) {

    final ActivatedJob activatedJob = activateJob(key, zeebeClient);
    zeebeClient
        .newFailCommand(activatedJob.getKey())
        .retries(0)
        .errorMessage("Failed by user.")
        .send()
        .join();
  }

  @RequestMapping(path = "/{key}/throw-error", method = RequestMethod.PUT)
  public void throwError(@PathVariable("key") long key, @RequestBody ThrowErrorDto dto) {

    zeebeClient.newThrowErrorCommand(key).errorCode(dto.getErrorCode()).send().join();
  }

  private ActivatedJob activateJob(long key, final ZeebeClient client) {
    final JobEntity job = getJob(key);
    final String jobType = job.getJobType();

    return activateJob(client, key, jobType);
  }

  private JobEntity getJob(long key) {
    return jobRepository
        .findByKey(key)
        .orElseThrow(() -> new RuntimeException("no job found with key: " + key));
  }

  private ActivatedJob activateJob(final ZeebeClient client, long key, final String jobType) {

    final List<ActivatedJob> jobs =
        client
            .newActivateJobsCommand()
            .jobType(jobType)
            .maxJobsToActivate(10)
            .timeout(Duration.ofSeconds(10))
            .workerName(WORKER_NAME)
            .send()
            .join()
            .getJobs();

    if (jobs.isEmpty()) {
      throw new RuntimeException("no activatable job found with key: " + key);
    } else {
      return jobs.stream()
          .filter(activatedJob -> activatedJob.getKey() == key)
          .findFirst()
          .orElseGet(() -> activateJob(client, key, jobType));
    }
  }
}
