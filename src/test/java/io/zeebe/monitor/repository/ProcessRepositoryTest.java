package io.zeebe.monitor.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.zeebe.monitor.entity.ProcessEntity;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessRepositoryTest extends ZeebeRepositoryTest {

  @Autowired private ProcessRepository processRepository;

  @AfterEach
  public void tearDown() {
    processRepository.deleteAll();
  }

  @Test
  public void when_process_exists_with_several_versions__then_loaded_only_latest() {
    // given
    ProcessEntity process1 = createProcess(101L, "A", 1);
    ProcessEntity process2 = createProcess(102L, "A", 2);

    // when
    processRepository.save(process1);
    processRepository.save(process2);

    // then
    List<Long> latestVersions = processRepository.findLatestVersions();
    assertThat(latestVersions).containsExactlyInAnyOrder(102L);
  }

  private ProcessEntity createProcess(long key, String bpmnProcessId, int version) {
    ProcessEntity entity = new ProcessEntity();
    entity.setKey(key);
    entity.setBpmnProcessId(bpmnProcessId);
    entity.setVersion(version);
    entity.setResource("123");
    entity.setTimestamp(456L);
    return entity;
  }
}
