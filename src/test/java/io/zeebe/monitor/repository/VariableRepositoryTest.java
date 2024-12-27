package io.zeebe.monitor.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.zeebe.monitor.entity.VariableEntity;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class VariableRepositoryTest extends ZeebeRepositoryTest {

  @Autowired private VariableRepository variableRepository;

  @Test
  public void JPA_will_automatically_update_the_ID_attribute() {
    // given
    VariableEntity variable = createVariable();

    // when
    variableRepository.save(variable);

    // then
    assertThat(variable.getId()).isEqualTo("123-456");
  }

  @Test
  public void variable_can_be_retrieved_by_transient_ID() {
    // given
    VariableEntity variable = createVariable();

    // when
    variableRepository.save(variable);

    // then
    Optional<VariableEntity> entity = variableRepository.findById("123-456");
    assertThat(entity).isPresent();
  }

  private VariableEntity createVariable() {
    VariableEntity variable = new VariableEntity();
    variable.setPartitionId(123);
    variable.setPosition(456L);
    return variable;
  }
}
