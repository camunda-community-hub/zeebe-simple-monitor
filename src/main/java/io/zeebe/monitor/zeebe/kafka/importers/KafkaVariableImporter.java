package io.zeebe.monitor.zeebe.kafka.importers;

import io.zeebe.monitor.entity.VariableEntity;
import io.zeebe.monitor.repository.VariableRepository;
import io.zeebe.monitor.zeebe.kafka.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaVariableImporter {

  @Autowired
  private VariableRepository variableRepository;

  public void importVariable(final GenericRecord record) {
    final VariableEntity newVariable = new VariableEntity();
    newVariable.setPosition(record.getPosition());
    newVariable.setPartitionId(record.getPartitionId());
    Map values = record.getValue();
    if (!variableRepository.existsById(newVariable.getGeneratedIdentifier())) {
      newVariable.setTimestamp(record.getTimestamp());
      newVariable.setProcessInstanceKey(values.get("processInstanceKey") != null ? ((Number)values.get("processInstanceKey")).longValue() : 0);
      newVariable.setName((String)values.get("name"));
      newVariable.setValue((String)values.get("value"));
      newVariable.setScopeKey(values.get("scopeKey") != null ? ((Number)values.get("scopeKey")).longValue() : 0);
      newVariable.setState(record.getIntent().toLowerCase());
      variableRepository.save(newVariable);
    }
  }

}
