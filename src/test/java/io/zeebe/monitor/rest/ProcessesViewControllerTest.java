package io.zeebe.monitor.rest;

import static io.zeebe.monitor.ZeebeSimpleMonitorApp.REPLACEMENT_CHARACTER_QUESTIONMARK;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ProcessesViewControllerTest extends AbstractViewOrResourceTest {

  @BeforeEach
  public void setUp() throws Exception {
    when(processRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
  }

  @Test
  public void index_page_successfully_responded() throws Exception {
    mockMvc.perform(get("/")).andExpect(status().isOk());
  }

  @Test
  public void index_page_contains_whitelabeling_title() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(content().string(containsString("Test Zeebe Simple Monitor")));
  }

  @Test
  public void index_page_contains_whitelabeling_logo() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(content().string(containsString("<img src=\"/img/test-logo.png\"")));
  }

  @Test
  public void index_page_contains_whitelabeling_js() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(content().string(containsString("<script src=\"/js/test-custom.js\"")));
  }

  @Test
  public void index_page_contains_whitelabeling_css() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(
            content()
                .string(
                    containsString(
                        "<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/test-custom.css\"/>")));
  }

  @Disabled
  @Test
  public void status_page_all_DTO_fields_in_template_can_be_resolved() throws Exception {
    mockMvc
        .perform(get("/views/processes"))
        .andExpect(content().string(not(containsString(REPLACEMENT_CHARACTER_QUESTIONMARK))));
  }

}
