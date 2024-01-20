package io.zeebe.monitor.zeebe.hazelcast.importers;

import io.camunda.zeebe.protocol.Protocol;
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
public class ProcessAndElementHazelcastImporter {

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

    final String intent = record.getMetadata().getIntent();
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

    if (intent.equalsIgnoreCase(ProcessInstanceIntent.ELEMENT_ACTIVATED.name())) {
      entity.setState(ProcessInstanceEntity.State.ACTIVE.getTitle());
      entity.setStart(timestamp);
      processInstanceRepository.save(entity);

      notificationService.sendCreatedProcessInstance(
          record.getProcessInstanceKey(), record.getProcessDefinitionKey());

    } else if (intent.equalsIgnoreCase(ProcessInstanceIntent.ELEMENT_COMPLETED.name())) {
      entity.setState(ProcessInstanceEntity.State.COMPLETED.getTitle());
      entity.setEnd(timestamp);
      processInstanceRepository.save(entity);

      notificationService.sendEndedProcessInstance(
          record.getProcessInstanceKey(), record.getProcessDefinitionKey());

    } else if (intent.equalsIgnoreCase(ProcessInstanceIntent.ELEMENT_TERMINATED.name())) {
      entity.setState(ProcessInstanceEntity.State.TERMINATED.getTitle());
      entity.setEnd(timestamp);
      processInstanceRepository.save(entity);

      notificationService.sendEndedProcessInstance(
          record.getProcessInstanceKey(), record.getProcessDefinitionKey());
    }
  }

  private void addElementInstance(final Schema.ProcessInstanceRecord record) {
    final ElementInstanceEntity entity = new ElementInstanceEntity();
    entity.setPartitionId(record.getMetadata().getPartitionId());
    entity.setPosition(record.getMetadata().getPosition());
    if (!elementInstanceRepository.existsById(entity.getGeneratedIdentifier())) {
      entity.setKey(record.getMetadata().getKey());
      entity.setIntent(record.getMetadata().getIntent());
      entity.setTimestamp(record.getMetadata().getTimestamp());
      entity.setProcessInstanceKey(record.getProcessInstanceKey());
      entity.setElementId(record.getElementId());
      entity.setFlowScopeKey(record.getFlowScopeKey());
      entity.setProcessDefinitionKey(record.getProcessDefinitionKey());
      entity.setBpmnElementType(record.getBpmnElementType());
      elementInstanceRepository.save(entity);
      notificationService.sendUpdatedProcessInstance(
          record.getProcessInstanceKey(), record.getProcessDefinitionKey());
    }
  }
}
