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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/messages")
public class MessageResource {

  @Autowired private ZeebeClient zeebeClient;

  @RequestMapping(path = "/", method = RequestMethod.POST)
  public void publishMessage(@RequestBody PublishMessageDto dto) {

    zeebeClient
        .newPublishMessageCommand()
        .messageName(dto.getName())
        .correlationKey(dto.getCorrelationKey())
        .variables(dto.getPayload())
        .timeToLive(Duration.parse(dto.getTimeToLive()))
        .send()
        .join();
  }
}
