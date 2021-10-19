package io.zeebe.monitor.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(
    classes = {TestContextJpaConfiguration.class},
    loader = AnnotationConfigContextLoader.class)
@Transactional
@ActiveProfiles({"postgres-docker", "application-junittest.yaml"})
public class ZeebeApplicationPostgresTest {

  @Autowired
  private VariableRepository variableRepository;

  @Test
  void setup_of_postgres_should_work() {
    assertThat(variableRepository).isNotNull();
  }
}
