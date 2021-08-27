package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.ProcessEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
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
import io.zeebe.monitor.zeebe.ZeebeHazelcastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
public class ViewControllerTest {

  @Autowired
  private ViewController controller;

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private HazelcastConfigRepository hazelcastConfigRepository;
  @MockBean
  private ZeebeHazelcastService zeebeHazelcastService;
  @MockBean
  private ProcessRepository processRepository;
  @MockBean
  private ProcessInstanceRepository processInstanceRepository;
  @MockBean
  private ElementInstanceRepository activityInstanceRepository;
  @MockBean
  private IncidentRepository incidentRepository;
  @MockBean
  private JobRepository jobRepository;
  @MockBean
  private MessageRepository messageRepository;
  @MockBean
  private MessageSubscriptionRepository messageSubscriptionRepository;
  @MockBean
  private TimerRepository timerRepository;
  @MockBean
  private VariableRepository variableRepository;
  @MockBean
  private ErrorRepository errorRepository;

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

	@Test
	public void testModelAttributesArePresentOnGetInstance() throws Exception {
		//GIVEN
		String bpmn = Files.readString(Paths.get(this.getClass().getClassLoader().getResource("orderProcess.bpmn").getPath()));

		ProcessInstanceEntity processInstanceEntity = mock(ProcessInstanceEntity.class, RETURNS_MOCKS);
		ProcessEntity processEntity = mock(ProcessEntity.class, RETURNS_MOCKS);
		when(processEntity.getResource()).thenReturn(bpmn);

		when(processInstanceRepository.findByKey(anyLong()))
				.thenReturn(Optional.of(processInstanceEntity));
		when(processRepository.findByKey(anyLong()))
				.thenReturn(Optional.of(processEntity));

		//WHEN
		mockMvc.perform(get("/views/instances/111"))
				//THEN
				.andExpect(model().attributeExists("context-path"))
				.andExpect(model().attributeExists("logo-path"))
				.andExpect(model().attributeExists("custom-css-path"))
				.andExpect(model().attributeExists("custom-js-path"))
				.andExpect(model().attributeExists("custom-title"));
	}

	@Test
	public void testModelAttributesPresentOnRuntimeException() throws Exception {
		//GIVEN
		// (this will throw a RuntimeException)
		when(processInstanceRepository.findByKey(anyLong()))
				.thenReturn(Optional.empty());

		//WHEN
		mockMvc.perform(get("/views/instances/111"))
				//THEN
				.andExpect(view().name("error"))
				.andExpect(model().attributeExists("context-path"))
				.andExpect(model().attributeExists("logo-path"))
				.andExpect(model().attributeExists("custom-css-path"))
				.andExpect(model().attributeExists("custom-js-path"))
				.andExpect(model().attributeExists("custom-title"));
	}


}
