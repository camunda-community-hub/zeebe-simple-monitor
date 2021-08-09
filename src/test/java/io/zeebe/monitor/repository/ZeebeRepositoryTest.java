package io.zeebe.monitor.repository;

import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    classes = {TestContextJpaConfiguration.class},
    loader = AnnotationConfigContextLoader.class)
@Transactional
@ActiveProfiles("junittest")
public abstract class ZeebeRepositoryTest {
  // all Repository Tests should inherit from this one...
}
