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

public class JobDto {

  private long key;
  private String jobType;
  private String state = "";
  private String worker = "";
  private int retries;

  private String elementId = "";
  private long elementInstanceKey;
  private long processInstanceKey;

  private String timestamp = "";

  private boolean isActivatable;

  public long getKey() {
    return key;
  }

  public void setKey(final long key) {
    this.key = key;
  }

  public String getJobType() {
    return jobType;
  }

  public void setJobType(final String jobType) {
    this.jobType = jobType;
  }

  public String getState() {
    return state;
  }

  public void setState(final String state) {
    this.state = state;
  }

  public String getWorker() {
    return worker;
  }

  public void setWorker(final String worker) {
    this.worker = worker;
  }

  public int getRetries() {
    return retries;
  }

  public void setRetries(final int retries) {
    this.retries = retries;
  }

  public String getElementId() {
    return elementId;
  }

  public void setElementId(final String elementId) {
    this.elementId = elementId;
  }

  public long getElementInstanceKey() {
    return elementInstanceKey;
  }

  public void setElementInstanceKey(final long elementInstanceKey) {
    this.elementInstanceKey = elementInstanceKey;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(final String timestamp) {
    this.timestamp = timestamp;
  }

  public long getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(final long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public boolean isActivatable() {
    return isActivatable;
  }

  public void setActivatable(final boolean isActivatable) {
    this.isActivatable = isActivatable;
  }
}
