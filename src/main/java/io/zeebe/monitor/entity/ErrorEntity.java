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

@Entity(name = "ERROR")
public class ErrorEntity {

  @Id
  @Column(name = "POSITION_")
  private long position;

  @Column(name = "ERROR_EVENT_POSITION_")
  private long errorEventPosition;

  @Column(name = "WORKFLOW_INSTANCE_KEY_")
  private long workflowInstanceKey;

  @Column(name = "EXCEPTION_MESSAGE_")
  @Lob
  private String exceptionMessage;

  @Column(name = "STACKTRACE_")
  @Lob
  private String stacktrace;

  @Column(name = "TIMESTAMP_")
  private long timestamp;

  public long getPosition() {
    return position;
  }

  public void setPosition(long position) {
    this.position = position;
  }

  public long getErrorEventPosition() {
    return errorEventPosition;
  }

  public void setErrorEventPosition(long errorEventPosition) {
    this.errorEventPosition = errorEventPosition;
  }

  public long getWorkflowInstanceKey() {
    return workflowInstanceKey;
  }

  public void setWorkflowInstanceKey(long workflowInstanceKey) {
    this.workflowInstanceKey = workflowInstanceKey;
  }

  public String getExceptionMessage() {
    return exceptionMessage;
  }

  public void setExceptionMessage(String exceptionMessage) {
    this.exceptionMessage = exceptionMessage;
  }

  public String getStacktrace() {
    return stacktrace;
  }

  public void setStacktrace(String stacktrace) {
    this.stacktrace = stacktrace;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
