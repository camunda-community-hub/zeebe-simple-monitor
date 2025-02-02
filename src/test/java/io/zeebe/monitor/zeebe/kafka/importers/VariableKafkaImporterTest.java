package io.zeebe.monitor.zeebe.kafka.importers;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.zeebe.protocol.record.ImmutableRecord;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.DeploymentIntent;
import io.camunda.zeebe.protocol.record.value.ImmutableVariableRecordValue;
import io.zeebe.monitor.config.BusinessKeyProperties;
import io.zeebe.monitor.entity.VariableEntity;
import io.zeebe.monitor.repository.VariableRepository;
import io.zeebe.monitor.repository.ZeebeRepositoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {VariableKafkaImporter.class, BusinessKeyProperties.class})
public class VariableKafkaImporterTest extends ZeebeRepositoryTest {

  @Autowired VariableKafkaImporter variableImporter;

  @Autowired VariableRepository variableRepository;

  @Test
  public void only_storing_first_variable_event_prevents_duplicate_PartitionID_and_Position() {
    // given
    Record<RecordValue> record1 = createVariableRecordWithName("first-variable");
    variableImporter.importRecord(record1);

    // when
    Record<RecordValue> record2 = createVariableRecordWithName("second-variable");
    variableImporter.importRecord(record2);

    // then
    Iterable<VariableEntity> all = variableRepository.findAll();
    assertThat(all).hasSize(1);
    assertThat(all.iterator().next().getName()).isEqualTo("first-variable");
  }

  private Record<RecordValue> createVariableRecordWithName(String name) {
    return buildVariableRecord(name).withRecordType(RecordType.EVENT).build();
  }

  private ImmutableRecord.Builder<RecordValue> buildVariableRecord(String name) {
    return ImmutableRecord.builder()
        .withValueType(ValueType.DEPLOYMENT)
        .withRecordType(RecordType.EVENT)
        .withTimestamp(System.currentTimeMillis())
        .withIntent(DeploymentIntent.CREATE)
        .withValue(ImmutableVariableRecordValue.builder().withName(name).build())
        .withPartitionId(1)
        .withPosition(1);
  }
}
