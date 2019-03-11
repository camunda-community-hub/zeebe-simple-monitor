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
import io.zeebe.monitor.zeebe.ZeebeConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instances")
public class WorkflowInstanceResource {

  @Autowired private ZeebeConnectionService connections;

  @RequestMapping(path = "/{key}", method = RequestMethod.DELETE)
  public void cancelWorkflowInstance(@PathVariable("key") long key) throws Exception {
    connections.getClient().newCancelInstanceCommand(key).send().join();
  }

  @RequestMapping(path = "/{key}/set-variables", method = RequestMethod.PUT)
  public void setVariables(@PathVariable("key") long key, @RequestBody String payload)
      throws Exception {
    connections.getClient().newSetVariablesCommand(key).variables(payload).send().join();
  }

  @RequestMapping(path = "/{key}/set-variables-local", method = RequestMethod.PUT)
  public void setVariablesLocal(@PathVariable("key") long key, @RequestBody String payload)
      throws Exception {
    connections
        .getClient()
        .newSetVariablesCommand(key)
        .variables(payload)
        .local(true)
        .send()
        .join();
  }

  @RequestMapping(path = "/{key}/resolve-incident", method = RequestMethod.PUT)
  public void resolveIncident(@PathVariable("key") long key, @RequestBody ResolveIncidentDto dto)
      throws Exception {

    final ZeebeClient client = connections.getClient();

    if (dto.getJobKey() != null && dto.getJobKey() > 0) {
      client
          .newUpdateRetriesCommand(dto.getJobKey())
          .retries(dto.getRemainingRetries())
          .send()
          .join();
    }

    client.newResolveIncidentCommand(key).send().join();
  }
}
