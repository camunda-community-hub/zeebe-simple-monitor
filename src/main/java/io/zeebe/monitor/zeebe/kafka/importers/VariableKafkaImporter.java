package io.zeebe.monitor.zeebe.kafka.importers;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.value.VariableRecordValue;
import io.zeebe.monitor.config.BusinessKeyProperties;
import io.zeebe.monitor.entity.ProcessInstanceBusinessKeyEntity;
import io.zeebe.monitor.entity.VariableEntity;
import io.zeebe.monitor.repository.ProcessInstanceBusinessKeyRepository;
import io.zeebe.monitor.repository.VariableRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VariableKafkaImporter extends KafkaImporter {

  @Autowired private VariableRepository variableRepository;
  @Autowired private ProcessInstanceBusinessKeyRepository processInstanceBusinessKeyRepository;
  @Autowired private BusinessKeyProperties businessKeyProperties;

  @Override
  @Transactional
  public void importRecord(final Record<RecordValue> record) {
    final var value = (VariableRecordValue) record.getValue();

    final var newVariable = new VariableEntity();
    newVariable.setPosition(record.getPosition());
    newVariable.setPartitionId(record.getPartitionId());
    if (!variableRepository.existsById(newVariable.getGeneratedIdentifier())) {
      newVariable.setTimestamp(record.getTimestamp());
      newVariable.setProcessInstanceKey(value.getProcessInstanceKey());
      newVariable.setName(value.getName());
      newVariable.setValue(value.getValue());
      newVariable.setScopeKey(value.getScopeKey());
      newVariable.setState(record.getIntent().name().toLowerCase());
      variableRepository.save(newVariable);

      if (businessKeyProperties.getFromVariablesKeys().contains(newVariable.getName())) {
        var businessKey = new ProcessInstanceBusinessKeyEntity();
        businessKey.setInstanceKey(newVariable.getProcessInstanceKey());
        businessKey.setBusinessKey(newVariable.getValue());

        processInstanceBusinessKeyRepository.save(businessKey);
      }
    }
  }
}
