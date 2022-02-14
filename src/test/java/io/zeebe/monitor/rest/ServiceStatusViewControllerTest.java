package io.zeebe.monitor.rest;

import io.camunda.zeebe.client.api.response.BrokerInfo;
import io.camunda.zeebe.client.api.response.Topology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
