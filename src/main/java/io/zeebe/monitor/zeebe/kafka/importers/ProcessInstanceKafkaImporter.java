package io.zeebe.monitor.zeebe.kafka.importers;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue;
import io.zeebe.monitor.entity.ElementInstanceEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.entity.ProcessInstanceState;
import io.zeebe.monitor.repository.ElementInstanceRepository;
import io.zeebe.monitor.repository.ProcessInstanceRepository;
import io.zeebe.monitor.zeebe.ZeebeNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessInstanceKafkaImporter extends KafkaImporter {

  @Autowired private ProcessInstanceRepository processInstanceRepository;
  @Autowired private ElementInstanceRepository elementInstanceRepository;
  @Autowired private ZeebeNotificationService notificationService;

  @Override
  public void importRecord(final Record<RecordValue> record) {
    final var value = (ProcessInstanceRecordValue) record.getValue();
    if (value.getProcessInstanceKey() == record.getKey()) {
      addOrUpdateProcessInstance(record);
    }
    addElementInstance(record);
  }

  private void addOrUpdateProcessInstance(final Record<RecordValue> record) {
    final var value = (ProcessInstanceRecordValue) record.getValue();
    final var intent = record.getIntent();
    final long timestamp = record.getTimestamp();
    final long processInstanceKey = value.getProcessInstanceKey();

    final var entity =
        processInstanceRepository
            .findById(processInstanceKey)
            .orElseGet(
                () -> {
                  final var newEntity = new ProcessInstanceEntity();
                  newEntity.setPartitionId(record.getPartitionId());
                  newEntity.setKey(processInstanceKey);
                  newEntity.setBpmnProcessId(value.getBpmnProcessId());
                  newEntity.setVersion(value.getVersion());
                  newEntity.setProcessDefinitionKey(value.getProcessDefinitionKey());
                  newEntity.setParentProcessInstanceKey(value.getParentProcessInstanceKey());
                  newEntity.setParentElementInstanceKey(value.getParentElementInstanceKey());
                  return newEntity;
                });

    if (intent == ProcessInstanceIntent.ELEMENT_ACTIVATED) {
      entity.setState(ProcessInstanceState.Active);
      entity.setStart(timestamp);
      processInstanceRepository.save(entity);

      notificationService.sendCreatedProcessInstance(
          value.getProcessInstanceKey(), value.getProcessDefinitionKey());

    } else if (intent == ProcessInstanceIntent.ELEMENT_COMPLETED) {
      entity.setState(ProcessInstanceState.Completed);
      entity.setEnd(timestamp);
      processInstanceRepository.save(entity);

      notificationService.sendEndedProcessInstance(
          value.getProcessInstanceKey(), value.getProcessDefinitionKey());

    } else if (intent == ProcessInstanceIntent.ELEMENT_TERMINATED) {
      entity.setState(ProcessInstanceState.Terminated);
      entity.setEnd(timestamp);
      processInstanceRepository.save(entity);

      notificationService.sendEndedProcessInstance(
          value.getProcessInstanceKey(), value.getProcessDefinitionKey());
    }
  }

  private void addElementInstance(final Record<RecordValue> record) {
    final var value = (ProcessInstanceRecordValue) record.getValue();

    final var entity = new ElementInstanceEntity();
    entity.setPartitionId(record.getPartitionId());
    entity.setPosition(record.getPosition());
    if (!elementInstanceRepository.existsById(entity.getGeneratedIdentifier())) {
      entity.setKey(record.getKey());
      entity.setIntent(record.getIntent().name());
      entity.setTimestamp(record.getTimestamp());
      entity.setProcessInstanceKey(value.getProcessInstanceKey());
      entity.setElementId(value.getElementId());
      entity.setFlowScopeKey(value.getFlowScopeKey());
      entity.setProcessDefinitionKey(value.getProcessDefinitionKey());
      entity.setBpmnElementType(
          value.getBpmnElementType() == null ? null : value.getBpmnElementType().name());
      elementInstanceRepository.save(entity);
      notificationService.sendUpdatedProcessInstance(
          value.getProcessInstanceKey(), value.getProcessDefinitionKey());
    }
  }
}
