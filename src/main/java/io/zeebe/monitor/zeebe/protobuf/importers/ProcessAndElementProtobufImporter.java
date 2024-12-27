package io.zeebe.monitor.zeebe.protobuf.importers;

import io.camunda.zeebe.protocol.Protocol;
import io.camunda.zeebe.protocol.record.intent.Intent;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
public class ProcessAndElementProtobufImporter {

  private final ProcessRepository processRepository;
  private final ProcessInstanceRepository processInstanceRepository;
  private final ElementInstanceRepository elementInstanceRepository;
  private final ZeebeNotificationService notificationService;
  private final Counter processCounter;
  private final Counter instanceActivatedCounter;
  private final Counter instanceCompletedCounter;
  private final Counter instanceTerminatedCounter;
  private final Counter elementInstanceCounter;

  @Autowired
  public ProcessAndElementProtobufImporter(
      ProcessRepository processRepository,
      ProcessInstanceRepository processInstanceRepository,
      ElementInstanceRepository elementInstanceRepository,
      MeterRegistry meterRegistry,
      ZeebeNotificationService notificationService) {
    this.processRepository = processRepository;
    this.processInstanceRepository = processInstanceRepository;
    this.elementInstanceRepository = elementInstanceRepository;
    this.notificationService = notificationService;

    this.processCounter =
        Counter.builder("zeebemonitor_importer_process")
            .description("number of processed processes")
            .register(meterRegistry);
    this.instanceActivatedCounter =
        Counter.builder("zeebemonitor_importer_process_instance")
            .tag("action", "activated")
            .description("number of activated process instances")
            .register(meterRegistry);
    this.instanceCompletedCounter =
        Counter.builder("zeebemonitor_importer_process_instance")
            .tag("action", "activated")
            .description("number of activated process instances")
            .register(meterRegistry);
    this.instanceTerminatedCounter =
        Counter.builder("zeebemonitor_importer_process_instance")
            .tag("action", "activated")
            .description("number of activated process instances")
            .register(meterRegistry);
    this.elementInstanceCounter =
        Counter.builder("zeebemonitor_importer_element_instance")
            .description("number of processed element_instances")
            .register(meterRegistry);
  }

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

    processCounter.increment();
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

      instanceActivatedCounter.increment();

    } else if (intent == ProcessInstanceIntent.ELEMENT_COMPLETED) {
      entity.setState("Completed");
      entity.setEnd(timestamp);
      processInstanceRepository.save(entity);

      notificationService.sendEndedProcessInstance(
          record.getProcessInstanceKey(), record.getProcessDefinitionKey());

      instanceCompletedCounter.increment();

    } else if (intent == ProcessInstanceIntent.ELEMENT_TERMINATED) {
      entity.setState("Terminated");
      entity.setEnd(timestamp);
      processInstanceRepository.save(entity);

      notificationService.sendEndedProcessInstance(
          record.getProcessInstanceKey(), record.getProcessDefinitionKey());

      instanceTerminatedCounter.increment();
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

      elementInstanceCounter.increment();
    }
  }
}
