package io.zeebe.monitor.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.camunda.zeebe.client.api.response.Topology;
import io.zeebe.monitor.repository.ElementInstanceRepository;
import io.zeebe.monitor.repository.ErrorRepository;
import io.zeebe.monitor.repository.HazelcastConfigRepository;
import io.zeebe.monitor.repository.IncidentRepository;
import io.zeebe.monitor.repository.JobRepository;
import io.zeebe.monitor.repository.MessageRepository;
import io.zeebe.monitor.repository.MessageSubscriptionRepository;
import io.zeebe.monitor.repository.ProcessInstanceRepository;
import io.zeebe.monitor.repository.ProcessRepository;
import io.zeebe.monitor.repository.TimerRepository;
import io.zeebe.monitor.repository.VariableRepository;
import io.zeebe.monitor.zeebe.hazelcast.ZeebeHazelcastService;
import io.zeebe.monitor.zeebe.status.ClusterStatus;
import io.zeebe.monitor.zeebe.status.ZeebeStatusKeeper;
import io.zeebe.monitor.zeebe.status.ZeebeStatusUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
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

  @LocalServerPort protected int port;
  @Autowired protected MockMvc mockMvc;
  @Autowired protected TestRestTemplate restTemplate;
  @Autowired protected InstancesViewController instancesViewController;
  @Autowired protected InstancesVariableListController instancesVariableListController;
  @Autowired protected ZeebeStatusKeeper zeebeStatusKeeper;

  @MockBean protected ZeebeStatusUpdateService zeebeStatusUpdateService;
  @MockBean protected HazelcastConfigRepository hazelcastConfigRepository;
  @MockBean protected ZeebeHazelcastService zeebeHazelcastService;
  @MockBean protected ProcessRepository processRepository;
  @MockBean protected ProcessInstanceRepository processInstanceRepository;
  @MockBean protected ElementInstanceRepository elementInstanceRepository;
  @MockBean protected IncidentRepository incidentRepository;
  @MockBean protected JobRepository jobRepository;
  @MockBean protected MessageRepository messageRepository;
  @MockBean protected MessageSubscriptionRepository messageSubscriptionRepository;
  @MockBean protected TimerRepository timerRepository;
  @MockBean protected VariableRepository variableRepository;
  @MockBean protected ErrorRepository errorRepository;

  protected void mockCLusterStatusForViews() {
    final Topology topologyMock = mock(Topology.class);
    ClusterStatus clusterStatus = new ClusterStatus();
    when(topologyMock.getGatewayVersion()).thenReturn("v9.9.9");
    clusterStatus.setTopology(topologyMock);
    zeebeStatusKeeper.setStatus(clusterStatus);
  }
}
