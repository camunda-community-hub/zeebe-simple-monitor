package io.zeebe.monitor.zeebe.kafka.importers;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.zeebe.protocol.record.ImmutableRecord;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.DeploymentIntent;
import io.camunda.zeebe.protocol.record.value.ImmutableProcessInstanceRecordValue;
import io.zeebe.monitor.entity.ElementInstanceEntity;
import io.zeebe.monitor.repository.ElementInstanceRepository;
import io.zeebe.monitor.repository.ZeebeRepositoryTest;
import io.zeebe.monitor.zeebe.ZeebeNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      ProcessKafkaImporter.class,
      ProcessInstanceKafkaImporter.class,
      ZeebeNotificationService.class
    })
public class ProcessKafkaImporterTest extends ZeebeRepositoryTest {

  @Autowired ProcessInstanceKafkaImporter processInstanceImporter;

  @Autowired ElementInstanceRepository elementInstanceRepository;

  @MockBean SimpMessagingTemplate simpMessagingTemplate;

  @Test
  public void only_storing_first_variable_event_prevents_duplicate_PartitionID_and_Position() {
    // given
    Record<RecordValue> processInstance1 = createElementInstanceWithId("first-elementId");
    processInstanceImporter.importRecord(processInstance1);

    // when
    Record<RecordValue> processInstance2 = createElementInstanceWithId("second-elementId");
    processInstanceImporter.importRecord(processInstance2);

    // then
    Iterable<ElementInstanceEntity> all = elementInstanceRepository.findAll();
    assertThat(all).hasSize(1);
    assertThat(all.iterator().next().getElementId()).isEqualTo("first-elementId");
  }

  private Record<RecordValue> createElementInstanceWithId(String elementId) {
    return buildDeploymentRecord(elementId).withRecordType(RecordType.EVENT).build();
  }

  private ImmutableRecord.Builder<RecordValue> buildDeploymentRecord(String elementId) {
    return ImmutableRecord.builder()
        .withValueType(ValueType.DEPLOYMENT)
        .withRecordType(RecordType.EVENT)
        .withTimestamp(System.currentTimeMillis())
        .withIntent(DeploymentIntent.CREATE)
        .withValue(ImmutableProcessInstanceRecordValue.builder().withElementId(elementId).build())
        .withPartitionId(1)
        .withPosition(1);
  }
}
