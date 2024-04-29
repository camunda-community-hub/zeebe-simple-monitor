package io.zeebe.monitor.zeebe.importers;

import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.ElementInstanceEntity;
import io.zeebe.monitor.model.BPMN_ELEMENT_TYPE;
import io.zeebe.monitor.repository.ElementInstanceRepository;
import io.zeebe.monitor.repository.ZeebeRepositoryTest;
import io.zeebe.monitor.zeebe.ZeebeNotificationService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(
    classes = {ProcessAndElementImporter.class,
        ZeebeNotificationService.class}
)
@Disabled("Broken with custom Postgres data types")
public class ProcessAndElementImporterTest extends ZeebeRepositoryTest {

  @Autowired
  ProcessAndElementImporter processAndElementImporter;

  @Autowired
  ElementInstanceRepository elementInstanceRepository;

  @MockBean
  SimpMessagingTemplate simpMessagingTemplate;

  @Test
  public void only_storing_first_variable_event_prevents_duplicate_PartitionID_and_Position() {
    // given
    Schema.ProcessInstanceRecord processInstance1 = createElementInstanceWithId("first-elementId");
    processAndElementImporter.importProcessInstance(processInstance1);

    // when
    Schema.ProcessInstanceRecord processInstance2 = createElementInstanceWithId("second-elementId");
    processAndElementImporter.importProcessInstance(processInstance2);

    // then
    Iterable<ElementInstanceEntity> all = elementInstanceRepository.findAll();
    assertThat(all).hasSize(1);
    assertThat(all.iterator().next().getElementId()).isEqualTo("first-elementId");
  }

  private Schema.ProcessInstanceRecord createElementInstanceWithId(String elementId) {
    return Schema.ProcessInstanceRecord.newBuilder()
        .setElementId(elementId)
        .setMetadata(Schema.RecordMetadata.newBuilder()
            .setPosition(333L)
            .setPartitionId(55555)
            .setIntent(ProcessInstanceIntent.ELEMENT_ACTIVATED.name()))
            .setBpmnElementType(BPMN_ELEMENT_TYPE.PROCESS.name())
        .build();
  }

}
