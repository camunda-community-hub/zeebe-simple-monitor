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

import javax.persistence.*;

@Entity(name = "VARIABLE")
@Table(indexes = {
    // performance reason, because we use it in the VariableRepository.findByProcessInstanceKey()
    @Index(name = "variable_processInstanceKeyIndex", columnList = "PROCESS_INSTANCE_KEY_"),
})
public class VariableEntity {

  @Id
  @Column(name = "ID")
  private String id;

  @Column(name = "POSITION_")
  private Long position;

  @Column(name = "PARTITION_ID_")
  private int partitionId;

  @Column(name = "NAME_")
  private String name;

  @Column(name = "VALUE_")
  @Lob
  private String value;

  @Column(name = "PROCESS_INSTANCE_KEY_")
  private long processInstanceKey;

  @Column(name = "SCOPE_KEY_")
  private long scopeKey;

  @Column(name = "STATE_")
  private String state;

  @Column(name = "TIMESTAMP_")
  private long timestamp;

  public String getId() {
    return id;
  }

  private void setId(final String id) {
    // made private, to avoid accidental changes
    this.id = id;
  }

  public final String getGeneratedIdentifier() {
    return this.partitionId + "-" + this.position;
  }

  @PrePersist
  private void prePersistDeriveIdField(){
    setId(getGeneratedIdentifier());
  }

  public String getState() {
    return state;
  }

  public void setState(final String state) {
    this.state = state;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(final long timestamp) {
    this.timestamp = timestamp;
  }

  public long getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(final long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(final String value) {
    this.value = value;
  }

  public long getScopeKey() {
    return scopeKey;
  }

  public void setScopeKey(final long scopeKey) {
    this.scopeKey = scopeKey;
  }

  public Long getPosition() {
    return position;
  }

  public void setPosition(final Long position) {
    this.position = position;
  }

  public int getPartitionId() {
    return partitionId;
  }

  public void setPartitionId(final int partitionId) {
    this.partitionId = partitionId;
  }
}
