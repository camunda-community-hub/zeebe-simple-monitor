package io.zeebe.monitor.zeebe.protobuf.importers;

import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.VariableEntity;
import io.zeebe.monitor.repository.VariableRepository;
import io.zeebe.monitor.zeebe.event.VariableEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class VariableProtobufImporter {

  private final VariableRepository variableRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public VariableProtobufImporter(
      VariableRepository variableRepository, ApplicationEventPublisher applicationEventPublisher) {
    this.variableRepository = variableRepository;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  public void importVariable(final Schema.VariableRecord record) {
    final VariableEntity newVariable = new VariableEntity();
    newVariable.setPosition(record.getMetadata().getPosition());
    newVariable.setPartitionId(record.getMetadata().getPartitionId());
    if (!variableRepository.existsById(newVariable.getGeneratedIdentifier())) {
      newVariable.setTimestamp(record.getMetadata().getTimestamp());
      newVariable.setProcessInstanceKey(record.getProcessInstanceKey());
      newVariable.setName(record.getName());
      newVariable.setValue(record.getValue());
      newVariable.setScopeKey(record.getScopeKey());
      newVariable.setState(record.getMetadata().getIntent().toLowerCase());
      variableRepository.save(newVariable);

      if (newVariable.getState().equals("updated")) {
        applicationEventPublisher.publishEvent(new VariableEvent(true));
      } else {
        applicationEventPublisher.publishEvent(new VariableEvent(false));
      }
    }
  }
}
