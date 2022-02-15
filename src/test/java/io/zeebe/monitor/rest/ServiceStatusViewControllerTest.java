package io.zeebe.monitor.rest;

import static io.zeebe.monitor.ZeebeSimpleMonitorApp.REPLACEMENT_CHARACTER_QUESTIONMARK;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import io.camunda.zeebe.client.api.response.BrokerInfo;
import io.camunda.zeebe.client.api.response.Topology;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServiceStatusViewControllerTest extends AbstractViewOrResourceTest {

  @BeforeEach
  public void setUp() throws Exception {
    when(zeebeStatusService.getTopology()).thenReturn(new TopologyMock());
  }

  @Test
  public void status_page_contains_default_version_string_if_not_packaged() throws Exception {
    mockMvc
        .perform(get("/views/service-status"))
        .andExpect(content().string(containsString("development build")))
        .andExpect(content().string(containsString("gateway-version")));
  }

  @Test
  public void status_page_all_DTO_fields_in_template_can_be_resolved() throws Exception {
    mockMvc
        .perform(get("/views/service-status"))
        .andExpect(content().string(not(containsString(REPLACEMENT_CHARACTER_QUESTIONMARK))));
  }

  private static class TopologyMock implements Topology {
    @Override
    public List<BrokerInfo> getBrokers() {
      return emptyList();
    }

    @Override
    public int getClusterSize() {
      return 1;
    }

    @Override
    public int getPartitionsCount() {
      return 2;
    }

    @Override
    public int getReplicationFactor() {
      return 3;
    }

    @Override
    public String getGatewayVersion() {
      return "gateway-version";
    }

  }
}
