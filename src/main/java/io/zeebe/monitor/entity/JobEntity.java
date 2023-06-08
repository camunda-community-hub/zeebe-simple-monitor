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

import jakarta.persistence.*;

@Entity(name = "JOB")
@Table(indexes = {
    // performance reason, because we use it in the
    // {@link io.zeebe.monitor.repository.JobRepository#findByProcessInstanceKey(long)}
    @Index(name = "job_processInstanceKeyIndex", columnList = "PROCESS_INSTANCE_KEY_"),
})
public class JobEntity {

  @Id
  @Column(name = "KEY_")
  private long key;

  @Column(name = "PROCESS_INSTANCE_KEY_")
  private long processInstanceKey;

  @Column(name = "ELEMENT_INSTANCE_KEY_")
  private long elementInstanceKey;

  @Column(name = "JOB_TYPE_")
  private String jobType;

  @Column(name = "WORKER_")
  private String worker;

  @Column(name = "STATE_")
  private String state;

  @Column(name = "RETRIES_")
  private int retries;

  @Column(name = "TIMESTAMP_")
  private long timestamp;

  public long getKey() {
    return key;
  }

  public void setKey(final long key) {
    this.key = key;
  }

  public long getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(final long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public long getElementInstanceKey() {
    return elementInstanceKey;
  }

  public void setElementInstanceKey(final long elementInstanceKey) {
    this.elementInstanceKey = elementInstanceKey;
  }

  public String getJobType() {
    return jobType;
  }

  public void setJobType(final String jobType) {
    this.jobType = jobType;
  }

  public String getWorker() {
    return worker;
  }

  public void setWorker(final String worker) {
    this.worker = worker;
  }

  public String getState() {
    return state;
  }

  public void setState(final String state) {
    this.state = state;
  }

  public int getRetries() {
    return retries;
  }

  public void setRetries(final int retries) {
    this.retries = retries;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(final long timestamp) {
    this.timestamp = timestamp;
  }
}
