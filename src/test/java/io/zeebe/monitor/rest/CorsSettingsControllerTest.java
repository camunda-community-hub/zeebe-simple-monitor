package io.zeebe.monitor.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.zeebe.monitor.repository.HazelcastConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "server.allowedOriginsUrls: http://www.someurl.com",
        "logging.level.io.zeebe.monitor: info",
    })
@AutoConfigureMockMvc
@ActiveProfiles("junittest")
public class CorsSettingsControllerTest {

  @LocalServerPort
  protected int port;
  @Autowired
  protected MockMvc mockMvc;

  @MockBean
  protected HazelcastConfigRepository hazelcastConfigRepository;


  @Test
  public void access_control_origin_request_header_is_checked() throws Exception {
    mockMvc.perform(
            options("/")
                .header("Access-Control-Request-Method", "GET")
                .header("Host", "localhost")
                .header("Origin", "http://a.bad-person.internet"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void access_control_allow_origin_response_header_is_send() throws Exception {
    mockMvc.perform(
            options("/")
                .header("Access-Control-Request-Method", "GET")
                .header("Host", "localhost")
                .header("Origin", "http://www.someurl.com"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://www.someurl.com"));
  }

}
