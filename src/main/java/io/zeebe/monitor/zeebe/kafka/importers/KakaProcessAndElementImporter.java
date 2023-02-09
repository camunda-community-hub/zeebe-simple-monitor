package io.zeebe.monitor.zeebe.kafka.importers;

import com.google.protobuf.ByteString;
import io.camunda.zeebe.protocol.Protocol;
import io.camunda.zeebe.protocol.record.intent.Intent;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.zeebe.monitor.entity.ElementInstanceEntity;
import io.zeebe.monitor.entity.ProcessEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.repository.ElementInstanceRepository;
import io.zeebe.monitor.repository.ProcessInstanceRepository;
import io.zeebe.monitor.repository.ProcessRepository;
import io.zeebe.monitor.zeebe.ZeebeNotificationService;
import io.zeebe.monitor.zeebe.kafka.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class KakaProcessAndElementImporter {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private ProcessRepository processRepository;
  @Autowired
  private ProcessInstanceRepository processInstanceRepository;
  @Autowired
  private ElementInstanceRepository elementInstanceRepository;

  @Autowired
  private ZeebeNotificationService notificationService;

  public void importProcess(final GenericRecord record) {
    final int partitionId = record.getPartitionId();

    if (partitionId != Protocol.DEPLOYMENT_PARTITION) {
      // ignore process event on other partitions to avoid duplicates
      return;
    }
    Map values = record.getValue();
    final ProcessEntity entity = new ProcessEntity();
    long processDefinitionKey = values.get("processDefinitionKey") != null ? ((Number)values.get("processDefinitionKey")).longValue():0;
    logger.info("processDefinitionKey: "+processDefinitionKey);
    entity.setKey(processDefinitionKey);
    entity.setBpmnProcessId((String)values.get("bpmnProcessId"));
    entity.setVersion(values.get("version") != null ? (int)values.get("version"):0);
    entity.setResource((String)values.get("resource"));
    entity.setTimestamp(record.getTimestamp());
    processRepository.save(entity);
    logger.info("importProcess done");
  }

  public void importProcessInstance(final GenericRecord record) {
    if (((Number)record.getValue().get("processInstanceKey")).longValue() == record.getKey()) {
      addOrUpdateProcessInstance(record);
      logger.info("addOrUpdateProcessInstance done");
    }
    addElementInstance(record);
    logger.info("addElementInstance done");
  }

  private void addOrUpdateProcessInstance(final GenericRecord record) {

    final Intent intent = ProcessInstanceIntent.valueOf(record.getIntent());
    final long timestamp = record.getTimestamp();
    final long processInstanceKey = record.getValue().get("processInstanceKey") != null ? ((Number)record.getValue().get("processInstanceKey")).longValue():0;
    Map values = record.getValue();
    final ProcessInstanceEntity entity =
        processInstanceRepository
            .findById(processInstanceKey)
            .orElseGet(
                () -> {
                  final ProcessInstanceEntity newEntity = new ProcessInstanceEntity();
                  newEntity.setPartitionId(record.getPartitionId());
                  newEntity.setKey(processInstanceKey);
                  newEntity.setBpmnProcessId((String)values.get("bpmnProcessId"));
                  newEntity.setVersion(values.get("version") != null ? (int)values.get("version"):0);
                  newEntity.setProcessDefinitionKey(values.get("processDefinitionKey") != null ? ((Number)values.get("processDefinitionKey")).longValue():0);
                  newEntity.setParentProcessInstanceKey(values.get("parentProcessInstanceKey") != null ? ((Number)values.get("parentProcessInstanceKey")).longValue():0);
                  newEntity.setParentElementInstanceKey(values.get("parentElementInstanceKey") != null ? ((Number)values.get("parentProcessInstanceKey")).longValue():0);
                  return newEntity;
                });

    if (intent == ProcessInstanceIntent.ELEMENT_ACTIVATED) {
      entity.setState("Active");
      entity.setStart(timestamp);
      processInstanceRepository.save(entity);

      notificationService.sendCreatedProcessInstance(
              ((Number) record.getValue().get("processInstanceKey")).longValue(), ((Number)record.getValue().get("processDefinitionKey")).longValue());

    } else if (intent == ProcessInstanceIntent.ELEMENT_COMPLETED) {
      entity.setState("Completed");
      entity.setEnd(timestamp);
      processInstanceRepository.save(entity);

      notificationService.sendEndedProcessInstance(
              ((Number) record.getValue().get("processInstanceKey")).longValue(), ((Number)record.getValue().get("processDefinitionKey")).longValue());

    } else if (intent == ProcessInstanceIntent.ELEMENT_TERMINATED) {
      entity.setState("Terminated");
      entity.setEnd(timestamp);
      processInstanceRepository.save(entity);

      notificationService.sendEndedProcessInstance(
              ((Number) record.getValue().get("processInstanceKey")).longValue(), ((Number)record.getValue().get("processDefinitionKey")).longValue());
    }
    logger.info("Process instance Import done");
  }

  private void addElementInstance(final GenericRecord record) {
    final ElementInstanceEntity entity = new ElementInstanceEntity();
    entity.setPartitionId(record.getPartitionId());
    entity.setPosition(record.getPosition());
    Map values = record.getValue();
    if (!elementInstanceRepository.existsById(entity.getGeneratedIdentifier())) {
      entity.setKey(record.getKey());
      entity.setIntent(record.getIntent());
      entity.setTimestamp(record.getTimestamp());
      entity.setProcessInstanceKey(values.get("processInstanceKey") != null ? ((Number)values.get("processInstanceKey")).longValue():0);
      entity.setElementId((String) values.get("elementId"));
      entity.setFlowScopeKey(values.get("flowScopeKey") != null ? ((Number)values.get("flowScopeKey")).longValue():0);
      entity.setProcessDefinitionKey(values.get("processDefinitionKey") != null ? ((Number)values.get("processDefinitionKey")).longValue():0);
      entity.setBpmnElementType((String)values.get("bpmnElementType"));
      elementInstanceRepository.save(entity);
      notificationService.sendUpdatedProcessInstance(((Number)values.get("processInstanceKey")).longValue(), ((Number)values.get("processDefinitionKey")).longValue());
    }
  }

}
