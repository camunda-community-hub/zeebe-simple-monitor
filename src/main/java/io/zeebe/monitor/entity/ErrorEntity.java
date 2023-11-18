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
import org.hibernate.Length;

@Entity(name = "ERROR")
@Table(indexes = {
    // performance reason, because we use it in the
    // {@link io.zeebe.monitor.repository.ErrorRepository#findByProcessInstanceKey(long)}
    @Index(name = "error_processInstanceKeyIndex", columnList = "PROCESS_INSTANCE_KEY_"),
})
public class ErrorEntity {

  @Id
  @Column(name = "POSITION_")
  private long position;

  @Column(name = "ERROR_EVENT_POSITION_")
  private long errorEventPosition;

  @Column(name = "PROCESS_INSTANCE_KEY_")
  private long processInstanceKey;

  @Column(name = "EXCEPTION_MESSAGE_",length= Length.LONG16)
  @Lob
  private String exceptionMessage;

  @Column(name = "STACKTRACE_",length= Length.LONG16)
  @Lob
  private String stacktrace;

  @Column(name = "TIMESTAMP_")
  private long timestamp;

  public long getPosition() {
    return position;
  }

  public void setPosition(final long position) {
    this.position = position;
  }

  public long getErrorEventPosition() {
    return errorEventPosition;
  }

  public void setErrorEventPosition(final long errorEventPosition) {
    this.errorEventPosition = errorEventPosition;
  }

  public long getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(final long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public String getExceptionMessage() {
    return exceptionMessage;
  }

  public void setExceptionMessage(final String exceptionMessage) {
    this.exceptionMessage = exceptionMessage;
  }

  public String getStacktrace() {
    return stacktrace;
  }

  public void setStacktrace(final String stacktrace) {
    this.stacktrace = stacktrace;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(final long timestamp) {
    this.timestamp = timestamp;
  }
}
