package io.zeebe.monitor.zeebe.importers;

import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.VariableEntity;
import io.zeebe.monitor.repository.VariableRepository;
import io.zeebe.monitor.repository.ZeebeRepositoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(
    classes = {VariableImporter.class}
)
public class VariableImporterTest extends ZeebeRepositoryTest {

  @Autowired
  VariableImporter variableImporter;

  @Autowired
  VariableRepository variableRepository;

  @Test
  public void only_storing_first_variable_event_prevents_duplicate_PartitionID_and_Position() {
    // given
    Schema.VariableRecord record1 = createVariableRecordWithName("first-variable");
    variableImporter.importVariable(record1);

    // when
    Schema.VariableRecord record2 = createVariableRecordWithName("second-variable");
    variableImporter.importVariable(record2);

    // then
    Iterable<VariableEntity> all = variableRepository.findAll();
    assertThat(all).hasSize(1);
    assertThat(all.iterator().next().getName()).isEqualTo("first-variable");
  }

  private Schema.VariableRecord createVariableRecordWithName(String name) {
    return Schema.VariableRecord.newBuilder()
        .setMetadata(Schema.RecordMetadata.newBuilder()
            .setPartitionId(123)
            .setPosition(456L))
        .setName(name)
        .build();
  }
}
