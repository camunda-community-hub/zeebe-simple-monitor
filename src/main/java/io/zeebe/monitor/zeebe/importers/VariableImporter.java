package io.zeebe.monitor.zeebe.importers;

import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.VariableEntity;
import io.zeebe.monitor.repository.VariableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VariableImporter {

  @Autowired private VariableRepository variableRepository;

  public void importVariable(final Schema.VariableRecord record) {

    final long position = record.getMetadata().getPosition();
    if (!variableRepository.existsById(position)) {

      final VariableEntity entity = new VariableEntity();
      entity.setPosition(position);
      entity.setTimestamp(record.getMetadata().getTimestamp());
      entity.setProcessInstanceKey(record.getProcessInstanceKey());
      entity.setName(record.getName());
      entity.setValue(record.getValue());
      entity.setScopeKey(record.getScopeKey());
      entity.setState(record.getMetadata().getIntent().toLowerCase());
      variableRepository.save(entity);
    }
  }

}
