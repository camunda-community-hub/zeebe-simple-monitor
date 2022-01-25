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
package io.zeebe.monitor.rest.dto;

import io.zeebe.monitor.entity.ProcessEntity;
import java.time.Instant;

public class ProcessDto {

  private long processDefinitionKey;
  private String bpmnProcessId;
  private int version;
  private String resource;
  private String deploymentTime;

  private long countRunning;
  private long countEnded;

  public static ProcessDto from(
      final ProcessEntity entity, final long countRunning, final long countEnded) {
    final ProcessDto dto = new ProcessDto();

    dto.processDefinitionKey = entity.getKey();
    dto.bpmnProcessId = entity.getBpmnProcessId();
    dto.version = entity.getVersion();
    dto.resource = entity.getResource();
    dto.deploymentTime = Instant.ofEpochMilli(entity.getTimestamp()).toString();

    dto.countRunning = countRunning;
    dto.countEnded = countEnded;

    return dto;
  }

  public long getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(final long processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getBpmnProcessId() {
    return bpmnProcessId;
  }

  public void setBpmnProcessId(final String bpmnProcessId) {
    this.bpmnProcessId = bpmnProcessId;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(final int version) {
    this.version = version;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(final String resource) {
    this.resource = resource;
  }

  public long getCountRunning() {
    return countRunning;
  }

  public void setCountRunning(final long countRunning) {
    this.countRunning = countRunning;
  }

  public long getCountEnded() {
    return countEnded;
  }

  public void setCountEnded(final long countEnded) {
    this.countEnded = countEnded;
  }

  public String getDeploymentTime() {
    return deploymentTime;
  }
}
