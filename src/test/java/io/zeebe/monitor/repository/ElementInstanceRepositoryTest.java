package io.zeebe.monitor.repository;

import io.zeebe.monitor.entity.ElementInstanceEntity;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ElementInstanceRepositoryTest extends ZeebeRepositoryTest {

  @Autowired
  private ElementInstanceRepository elementInstanceRepository;

  @Test
  public void JPA_will_automatically_update_the_ID_attribute() {
    // given
    ElementInstanceEntity elementInstance = createElementInstance();

    // when
    elementInstanceRepository.save(elementInstance);

    // then
    assertThat(elementInstance.getId()).isEqualTo("123-456");
  }

  @Test
  public void variable_can_be_retrieved_by_transient_ID() {
    // given
    ElementInstanceEntity elementInstance = createElementInstance();

    // when
    elementInstanceRepository.save(elementInstance);

    // then
    Optional<ElementInstanceEntity> entity = elementInstanceRepository.findById("123-456");
    assertThat(entity).isPresent();
  }

  private ElementInstanceEntity createElementInstance() {
    ElementInstanceEntity elementInstance = new ElementInstanceEntity();
    elementInstance.setPartitionId(123);
    elementInstance.setPosition(456L);
    return elementInstance;
  }

}
