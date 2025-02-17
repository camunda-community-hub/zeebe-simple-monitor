package io.zeebe.monitor.zeebe.protobuf.importers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.config.BusinessKeyProperties;
import io.zeebe.monitor.entity.ProcessInstanceBusinessKeyEntity;
import io.zeebe.monitor.entity.VariableEntity;
import io.zeebe.monitor.repository.ProcessInstanceBusinessKeyRepository;
import io.zeebe.monitor.repository.VariableRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VariableProtobufImporter {

  private final VariableRepository variableRepository;
  private final ProcessInstanceBusinessKeyRepository processInstanceBusinessKeyRepository;
  private final BusinessKeyProperties businessKeyProperties;
  private final Counter variableCreatedCounter;
  private final Counter variableUpdatedCounter;

  @Autowired
  public VariableProtobufImporter(
      VariableRepository variableRepository,
      ProcessInstanceBusinessKeyRepository processInstanceBusinessKeyRepository,
      BusinessKeyProperties businessKeyProperties,
      MeterRegistry meterRegistry) {
    this.variableRepository = variableRepository;
    this.processInstanceBusinessKeyRepository = processInstanceBusinessKeyRepository;
    this.businessKeyProperties = businessKeyProperties;

    this.variableCreatedCounter =
        Counter.builder("zeebemonitor_importer_variable")
            .tag("action", "imported")
            .description("number of processed variables")
            .register(meterRegistry);
    this.variableUpdatedCounter =
        Counter.builder("zeebemonitor_importer_variable")
            .tag("action", "updated")
            .description("number of processed variables")
            .register(meterRegistry);
  }

  @Transactional
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

      if (businessKeyProperties.getFromVariablesKeys().contains(newVariable.getName())) {
        var businessKey = new ProcessInstanceBusinessKeyEntity();
        businessKey.setInstanceKey(newVariable.getProcessInstanceKey());
        businessKey.setBusinessKey(newVariable.getValue());

        processInstanceBusinessKeyRepository.save(businessKey);
      }

      if (newVariable.getState().equals("updated")) {
        variableUpdatedCounter.increment();
      } else {
        variableCreatedCounter.increment();
      }
    }
  }
}
