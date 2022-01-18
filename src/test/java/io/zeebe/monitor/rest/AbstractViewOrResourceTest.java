package io.zeebe.monitor.rest;

import io.zeebe.monitor.repository.*;
import io.zeebe.monitor.zeebe.ZeebeHazelcastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "white-label.logo.path: img/test-logo.png",
        "white-label.custom.title: Test Zeebe Simple Monitor",
        "white-label.custom.css.path: css/test-custom.css",
        "white-label.custom.js.path: js/test-custom.js",
        "logging.level.io.zeebe.monitor: info",
    })
@AutoConfigureMockMvc
@ActiveProfiles("junittest")
public abstract class AbstractViewOrResourceTest {

  @LocalServerPort
  protected int port;
  @Autowired
  protected MockMvc mockMvc;
  @Autowired
  protected TestRestTemplate restTemplate;
  @Autowired
  protected ViewController controller;


  @MockBean
  protected HazelcastConfigRepository hazelcastConfigRepository;
  @MockBean
  protected ZeebeHazelcastService zeebeHazelcastService;
  @MockBean
  protected ProcessRepository processRepository;
  @MockBean
  protected ProcessInstanceRepository processInstanceRepository;
  @MockBean
  protected ElementInstanceRepository activityInstanceRepository;
  @MockBean
  protected IncidentRepository incidentRepository;
  @MockBean
  protected JobRepository jobRepository;
  @MockBean
  protected MessageRepository messageRepository;
  @MockBean
  protected MessageSubscriptionRepository messageSubscriptionRepository;
  @MockBean
  protected TimerRepository timerRepository;
  @MockBean
  protected VariableRepository variableRepository;
  @MockBean
  protected ErrorRepository errorRepository;

}
