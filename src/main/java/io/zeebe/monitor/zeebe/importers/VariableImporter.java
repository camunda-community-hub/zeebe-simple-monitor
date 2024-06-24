package io.zeebe.monitor.zeebe.importers;

import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.VariableEntity;
import io.zeebe.monitor.repository.VariableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class VariableImporter {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessAndElementImporter.class);

    @Autowired
    private VariableRepository variableRepository;

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
            try {
                variableRepository.save(newVariable);
            } catch (DataIntegrityViolationException e) {
                LOG.warn("Attempted to save duplicate Element Instance with id {}", newVariable.getGeneratedIdentifier());
            }
        }
    }

}
