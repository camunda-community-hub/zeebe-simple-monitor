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

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instances")
public class ProcessInstanceResource {

  @Autowired private ZeebeClient zeebeClient;

  @RequestMapping(path = "/{key}", method = RequestMethod.DELETE)
  public void cancelProcessInstance(@PathVariable("key") final long key) throws Exception {
    zeebeClient.newCancelInstanceCommand(key).send().join();
  }

  @RequestMapping(path = "/{key}/set-variables", method = RequestMethod.PUT)
  public void setVariables(@PathVariable("key") final long key, @RequestBody final String payload)
      throws Exception {
    zeebeClient.newSetVariablesCommand(key).variables(payload).send().join();
  }

  @RequestMapping(path = "/{key}/set-variables-local", method = RequestMethod.PUT)
  public void setVariablesLocal(
      @PathVariable("key") final long key, @RequestBody final String payload) throws Exception {
    zeebeClient.newSetVariablesCommand(key).variables(payload).local(true).send().join();
  }

  @RequestMapping(path = "/{key}/resolve-incident", method = RequestMethod.PUT)
  public void resolveIncident(
      @PathVariable("key") final long key, @RequestBody final ResolveIncidentDto dto)
      throws Exception {

    if (dto.getJobKey() != null && dto.getJobKey() > 0) {
      zeebeClient
          .newUpdateRetriesCommand(dto.getJobKey())
          .retries(dto.getRemainingRetries())
          .send()
          .join();
    }

    zeebeClient.newResolveIncidentCommand(key).send().join();
  }
}
