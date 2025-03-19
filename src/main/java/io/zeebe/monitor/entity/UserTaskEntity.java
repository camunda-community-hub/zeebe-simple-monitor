package io.zeebe.monitor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity(name = "USER_TASK")
@Table(
    indexes = {
      @Index(name = "user_task_processInstanceKeyIndex", columnList = "PROCESS_INSTANCE_KEY_"),
      @Index(name = "user_task_bpmnProcessIdIndex", columnList = "BPMN_PROCESS_ID_"),
      @Index(name = "user_task_statusIndex", columnList = "STATUS"),
      @Index(name = "user_task_elementIdIndex", columnList = "ELEMENT_ID_"),
    })
public class UserTaskEntity {

  @Id
  @Column(name = "KEY_")
  private long key;

  @Column(name = "PROCESS_INSTANCE_KEY_")
  private long processInstanceKey;

  @Column(name = "ELEMENT_INSTANCE_KEY_")
  private long elementInstanceKey;

  @Column(name = "ELEMENT_ID_")
  private String elementId;

  @Column(name = "STATUS")
  private String status;

  @Column(name = "BPMN_PROCESS_ID_")
  private String bpmnProcessId;

  @Column(name = "START_")
  private Long start;

  @Column(name = "END_")
  private Long end;

  @Transient private boolean notActive;

  public long getKey() {
    return key;
  }

  public void setKey(long key) {
    this.key = key;
  }

  public long getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public long getElementInstanceKey() {
    return elementInstanceKey;
  }

  public void setElementInstanceKey(long elementInstanceKey) {
    this.elementInstanceKey = elementInstanceKey;
  }

  public String getElementId() {
    return elementId;
  }

  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getBpmnProcessId() {
    return bpmnProcessId;
  }

  public void setBpmnProcessId(String bpmnProcessId) {
    this.bpmnProcessId = bpmnProcessId;
  }

  public Long getStart() {
    return start;
  }

  public void setStart(Long start) {
    this.start = start;
  }

  public Long getEnd() {
    return end;
  }

  public void setEnd(Long end) {
    this.end = end;
  }

  public boolean isNotActive() {
    return notActive = this.status != "Active";
  }
}
