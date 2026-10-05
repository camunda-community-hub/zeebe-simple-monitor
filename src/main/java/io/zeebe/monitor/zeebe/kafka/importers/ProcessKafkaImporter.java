package io.zeebe.monitor.zeebe.kafka.importers;

import io.camunda.zeebe.protocol.Protocol;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.zeebe.monitor.entity.ProcessEntity;
import io.zeebe.monitor.repository.ProcessRepository;
import io.zeebe.monitor.zeebe.event.ProcessEvent;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ProcessKafkaImporter extends KafkaImporter {

  @Autowired private ProcessRepository processRepository;
  @Autowired private ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void importRecord(final Record<RecordValue> record) {
    final var value = (io.camunda.zeebe.protocol.record.value.deployment.Process) record.getValue();
    final int partitionId = record.getPartitionId();

    if (partitionId != Protocol.DEPLOYMENT_PARTITION) {
      // ignore process event on other partitions to avoid duplicates
      return;
    }

    final var entity = new ProcessEntity();
    entity.setKey(value.getProcessDefinitionKey());
    entity.setBpmnProcessId(value.getBpmnProcessId());
    entity.setVersion(value.getVersion());
    entity.setResource(new String(value.getResource(), StandardCharsets.UTF_8));
    entity.setTimestamp(record.getTimestamp());
    processRepository.save(entity);

    applicationEventPublisher.publishEvent(new ProcessEvent());
  }
}
