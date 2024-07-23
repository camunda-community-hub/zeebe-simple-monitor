package io.zeebe.monitor.zeebe.importers;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import static io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent.*;

@Component
public class ProcessAndElementImporter {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessAndElementImporter.class);

    private final ProcessRepository processRepository;
    private final ProcessInstanceRepository processInstanceRepository;
    private final ElementInstanceRepository elementInstanceRepository;

    private final ZeebeNotificationService notificationService;
    private ProcessEntity newEntity;

    public ProcessAndElementImporter(ProcessRepository processRepository, ProcessInstanceRepository processInstanceRepository, ElementInstanceRepository elementInstanceRepository, ZeebeNotificationService notificationService) {
        this.processRepository = processRepository;
        this.processInstanceRepository = processInstanceRepository;
        this.elementInstanceRepository = elementInstanceRepository;
        this.notificationService = notificationService;
    }

    public void importProcess(final Schema.ProcessRecord record) {
        final int partitionId = record.getMetadata().getPartitionId();

        final ProcessEntity entity =
                processRepository.findById(record.getProcessDefinitionKey()).orElseGet(
                        () -> {
                            final ProcessEntity newEntity = new ProcessEntity();
                            newEntity.setKey(record.getProcessDefinitionKey());
                            newEntity.setBpmnProcessId(record.getBpmnProcessId());
                            newEntity.setVersion(record.getVersion());
                            newEntity.setResource(record.getResource().toStringUtf8());
                            newEntity.setTimestamp(record.getMetadata().getTimestamp());
                            return newEntity;
                        });
        try {
            processRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            LOG.warn("Attempted to save duplicate Process with key {}", entity.getKey());
        }
    }

    public void importProcessInstance(final Schema.ProcessInstanceRecord record) {
        if (record.getProcessInstanceKey() == record.getMetadata().getKey()) {
            addOrUpdateProcessInstance(record);
        }
        addElementInstance(record);
    }

    private void addOrUpdateProcessInstance(final Schema.ProcessInstanceRecord record) {

        final Intent intent = valueOf(record.getMetadata().getIntent());
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

        switch (intent) {
            case ELEMENT_ACTIVATED -> {
                entity.setState("Active");
                entity.setStart(timestamp);
                saveProcessInstanceRecord(entity);

                notificationService.sendCreatedProcessInstance(
                        record.getProcessInstanceKey(), record.getProcessDefinitionKey());
            }
            case ELEMENT_COMPLETED -> {
                entity.setState("Completed");
                entity.setEnd(timestamp);
                saveProcessInstanceRecord(entity);

                notificationService.sendEndedProcessInstance(
                        record.getProcessInstanceKey(), record.getProcessDefinitionKey());
            }
            case ELEMENT_TERMINATED -> {
                entity.setState("Terminated");
                entity.setEnd(timestamp);
                saveProcessInstanceRecord(entity);

                notificationService.sendEndedProcessInstance(
                        record.getProcessInstanceKey(), record.getProcessDefinitionKey());
            }
            default -> {}
        }
    }

    private void saveProcessInstanceRecord(ProcessInstanceEntity pei){
        try {
            processInstanceRepository.save(pei);
        } catch (DataIntegrityViolationException e) {
            LOG.warn("Attempted to save duplicate ProcessInstance with key {}", pei.getKey());
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
            try {
                elementInstanceRepository.save(entity);
            } catch (DataIntegrityViolationException e) {
                LOG.warn("Attempted to save duplicate Element Instance with id {}", entity.getGeneratedIdentifier());
            }
            notificationService.sendUpdatedProcessInstance(record.getProcessInstanceKey(), record.getProcessDefinitionKey());
        }
    }

}