package io.zeebe.monitor.zeebe.protobuf.importers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.VariableEntity;
import io.zeebe.monitor.repository.VariableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VariableProtobufImporter {

  private final VariableRepository variableRepository;
  private final Counter variableCreatedCounter;
  private final Counter variableUpdatedCounter;

  public VariableHazelcastImporter(VariableRepository variableRepository, MeterRegistry meterRegistry) {
    this.variableRepository = variableRepository;

    this.variableCreatedCounter = Counter.builder("zeebemonitor_importer_variable").tag("action", "imported").description("number of processed variables").register(meterRegistry);
    this.variableUpdatedCounter = Counter.builder("zeebemonitor_importer_variable").tag("action", "updated").description("number of processed variables").register(meterRegistry);
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
        variableUpdatedCounter.increment();
      } else {
        variableCreatedCounter.increment();
      }
    }
  }
}
