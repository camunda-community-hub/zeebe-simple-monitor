package io.zeebe.monitor.zeebe.importers;

import io.camunda.zeebe.protocol.Protocol;
import io.camunda.zeebe.protocol.record.intent.Intent;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.ElementInstanceEntity;
import io.zeebe.monitor.entity.ProcessEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.repository.ElementInstanceRepository;
import io.zeebe.monitor.repository.ProcessInstanceRepository;
import io.zeebe.monitor.repository.ProcessRepository;
import io.zeebe.monitor.zeebe.ZeebeNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessAndElementImporter {

  @Autowired private ProcessRepository processRepository;
  @Autowired private ProcessInstanceRepository processInstanceRepository;
  @Autowired private ElementInstanceRepository elementInstanceRepository;

  @Autowired private ZeebeNotificationService notificationService;

  public void importProcess(final Schema.ProcessRecord record) {
    final int partitionId = record.getMetadata().getPartitionId();

    if (partitionId != Protocol.DEPLOYMENT_PARTITION) {
      // ignore process event on other partitions to avoid duplicates
      return;
    }

    final ProcessEntity entity = new ProcessEntity();
    entity.setKey(record.getProcessDefinitionKey());
    entity.setBpmnProcessId(record.getBpmnProcessId());
    entity.setVersion(record.getVersion());
    entity.setResource(record.getResource().toStringUtf8());
    entity.setTimestamp(record.getMetadata().getTimestamp());
    processRepository.save(entity);
  }

  public void importProcessInstance(final Schema.ProcessInstanceRecord record) {
    if (record.getProcessInstanceKey() == record.getMetadata().getKey()) {
      addOrUpdateProcessInstance(record);
    }

    addElementInstance(record);
  }

  private void addOrUpdateProcessInstance(final Schema.ProcessInstanceRecord record) {

    final Intent intent = ProcessInstanceIntent.valueOf(record.getMetadata().getIntent());
    final long timestamp = record.getMetadata().getTimestamp();
    final long processInstanceKey = record.getProcessInstanceKey();

    final ProcessInstanceEntity entity =
        processInstanceRepository
            .findById(processInstanceKey)
            .orElseGet(
                () -> {
                  final ProcessInstanceEntity newEntity = new ProcessInstanceEntity();
                  newEntity.setPartitionId(record.getMetadata().getPartitionId());
                  newEntity.setKey(processInstanceKey);
                  newEntity.setBpmnProcessId(record.getBpmnProcessId());
                  newEntity.setVersion(record.getVersion());
                  newEntity.setProcessDefinitionKey(record.getProcessDefinitionKey());
                  newEntity.setParentProcessInstanceKey(record.getParentProcessInstanceKey());
                  newEntity.setParentElementInstanceKey(record.getParentElementInstanceKey());
                  return newEntity;
                });

    if (intent == ProcessInstanceIntent.ELEMENT_ACTIVATED) {
      entity.setState("Active");
      entity.setStart(timestamp);
      processInstanceRepository.save(entity);

      notificationService.sendCreatedProcessInstance(
          record.getProcessInstanceKey(), record.getProcessDefinitionKey());

    } else if (intent == ProcessInstanceIntent.ELEMENT_COMPLETED) {
      entity.setState("Completed");
      entity.setEnd(timestamp);
      processInstanceRepository.save(entity);

      notificationService.sendEndedProcessInstance(
          record.getProcessInstanceKey(), record.getProcessDefinitionKey());

    } else if (intent == ProcessInstanceIntent.ELEMENT_TERMINATED) {
      entity.setState("Terminated");
      entity.setEnd(timestamp);
      processInstanceRepository.save(entity);

      notificationService.sendEndedProcessInstance(
          record.getProcessInstanceKey(), record.getProcessDefinitionKey());
    }
  }

  private void addElementInstance(final Schema.ProcessInstanceRecord record) {

    final long position = record.getMetadata().getPosition();
    if (!elementInstanceRepository.existsById(position)) {

      final ElementInstanceEntity entity = new ElementInstanceEntity();
      entity.setPosition(position);
      entity.setPartitionId(record.getMetadata().getPartitionId());
      entity.setKey(record.getMetadata().getKey());
      entity.setIntent(record.getMetadata().getIntent());
      entity.setTimestamp(record.getMetadata().getTimestamp());
      entity.setProcessInstanceKey(record.getProcessInstanceKey());
      entity.setElementId(record.getElementId());
      entity.setFlowScopeKey(record.getFlowScopeKey());
      entity.setProcessDefinitionKey(record.getProcessDefinitionKey());
      entity.setBpmnElementType(record.getBpmnElementType());

      elementInstanceRepository.save(entity);

      notificationService.sendProcessInstanceUpdated(
          record.getProcessInstanceKey(), record.getProcessDefinitionKey());
    }
  }

}
