package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.ProcessEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProcessesViewControllerTest extends AbstractViewOrResourceTest {

  @BeforeEach
  public void setUp() throws Exception {
	  when(processRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
  }

	@Test
	public void index_page_successfully_responded() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk());
	}

	@Test
	public void index_page_contains_whitelabeling_title() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(content().string(containsString("Test Zeebe Simple Monitor")));
	}

	@Test
	public void index_page_contains_whitelabeling_logo() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(content().string(containsString("<img src=\"/img/test-logo.png\"")));
	}

	@Test
	public void index_page_contains_whitelabeling_js() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(content().string(containsString("<script src=\"/js/test-custom.js\"")));

	}

	@Test
	public void index_page_contains_whitelabeling_css() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(content().string(containsString("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/test-custom.css\"/>")));
	}

}
