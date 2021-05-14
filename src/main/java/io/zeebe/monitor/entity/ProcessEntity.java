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
package io.zeebe.monitor.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity(name = "PROCESS")
public class ProcessEntity {
  @Id
  @Column(name = "KEY_")
  private long key;

  @Column(name = "BPMN_PROCESS_ID_")
  private String bpmnProcessId;

  @Column(name = "VERSION_")
  private int version;

  @Lob
  @Column(name = "RESOURCE_")
  private String resource;

  @Column(name = "TIMESTAMP_")
  private long timestamp;

  public String getResource() {
    return resource;
  }

  public void setResource(final String resource) {
    this.resource = resource;
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

  public long getKey() {
    return key;
  }

  public void setKey(final long key) {
    this.key = key;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(final long timestamp) {
    this.timestamp = timestamp;
  }
}
