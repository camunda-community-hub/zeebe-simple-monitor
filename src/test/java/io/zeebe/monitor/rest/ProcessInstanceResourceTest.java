package io.zeebe.monitor.rest;

import io.zeebe.monitor.zeebe.ZeebeNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.FAILED_DEPENDENCY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProcessInstanceResourceTest extends AbstractViewOrResourceTest {

  @Autowired
  protected ProcessInstanceResource processInstanceResource;

  @MockBean()
  private ZeebeNotificationService zeebeNotificationServiceMock;

  @Test
  void resolve_incident_command_is_send_even_when_prior_change_job_retries_command_fails() throws Exception {
    // given
    final ResolveIncidentDto dto = new ResolveIncidentDto();
    dto.setIncidentKey(123456789);
    dto.setJobKey(999L);
    dto.setRemainingRetries(1);

    // when & then
    this.mockMvc.perform(
        put("/api/instances/{key}/resolve-incident", 123456789)
            .contentType(MediaType.APPLICATION_JSON)
            .content(createDummyRequestBody(dto))
    ).andExpect(status().is(FAILED_DEPENDENCY.value()));

    // then
    verify(zeebeNotificationServiceMock).sendZeebeClusterError(anyString());
  }

  private byte[] createDummyRequestBody(Object object) throws IOException {
    try (final ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
      new ObjectMapper().writeValue(buffer, object);
      return buffer.toByteArray();
    }
  }

}
