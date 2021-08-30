package io.zeebe.monitor.repository;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ContextConfiguration(
    classes = {TestContextJpaConfiguration.class},
    loader = AnnotationConfigContextLoader.class)
@Transactional
@ActiveProfiles("junittest")
public abstract class ZeebeRepositoryTest {
  // all Repository Tests should inherit from this one...
}
