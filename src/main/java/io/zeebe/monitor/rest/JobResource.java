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
import io.zeebe.monitor.rest.dto.ThrowErrorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobResource {

  @Autowired private ZeebeClient zeebeClient;

  @RequestMapping(path = "/{key}/complete", method = RequestMethod.PUT)
  public void completeJob(
      @PathVariable("key") final long key, @RequestBody final String variables) {

    zeebeClient.newCompleteCommand(key).variables(variables).send().join();
  }

  @RequestMapping(path = "/{key}/fail", method = RequestMethod.PUT)
  public void failJob(@PathVariable("key") final long key) {

    zeebeClient.newFailCommand(key).retries(0).errorMessage("Failed by user.").send().join();
  }

  @RequestMapping(path = "/{key}/throw-error", method = RequestMethod.PUT)
  public void throwError(
      @PathVariable("key") final long key, @RequestBody final ThrowErrorDto dto) {

    zeebeClient.newThrowErrorCommand(key).errorCode(dto.getErrorCode()).send().join();
  }
}
