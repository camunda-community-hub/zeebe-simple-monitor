package io.zeebe.monitor.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import io.zeebe.monitor.entity.ProcessEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class InstancesVariableListControllerTest extends AbstractViewOrResourceTest {

  @BeforeEach
  public void setUp() {
    when(processRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
    when(elementInstanceRepository.findByProcessInstanceKey(anyLong())).thenReturn(Page.empty());
    when(variableRepository.findByProcessInstanceKey(anyLong(), any(Pageable.class)))
        .thenReturn(Page.empty());
  }

  @Test
  public void testModelAttributesArePresentOnGetInstance() throws Exception {
    // GIVEN
    String bpmn =
        Files.readString(
            Paths.get(this.getClass().getClassLoader().getResource("orderProcess.bpmn").getPath()));

    ProcessInstanceEntity processInstanceEntity = mock(ProcessInstanceEntity.class, RETURNS_MOCKS);
    ProcessEntity processEntity = mock(ProcessEntity.class, RETURNS_MOCKS);
    when(processEntity.getResource()).thenReturn(bpmn);

    when(processInstanceRepository.findByKey(anyLong()))
        .thenReturn(Optional.of(processInstanceEntity));
    when(processRepository.findByKey(anyLong())).thenReturn(Optional.of(processEntity));

    // WHEN
    mockMvc
        .perform(get("/views/instances/111"))
        // THEN
        .andExpect(model().attributeExists("context-path"))
        .andExpect(model().attributeExists("logo-path"))
        .andExpect(model().attributeExists("custom-css-path"))
        .andExpect(model().attributeExists("custom-js-path"))
        .andExpect(model().attributeExists("custom-title"));
  }

  @Test
  public void testModelAttributesPresentOnRuntimeException() throws Exception {
    // GIVEN
    // (this will throw a RuntimeException)
    when(processInstanceRepository.findByKey(anyLong())).thenReturn(Optional.empty());

    // WHEN
    mockMvc
        .perform(get("/views/instances/111"))
        // THEN
        .andExpect(view().name("error"))
        .andExpect(model().attributeExists("context-path"))
        .andExpect(model().attributeExists("logo-path"))
        .andExpect(model().attributeExists("custom-css-path"))
        .andExpect(model().attributeExists("custom-js-path"))
        .andExpect(model().attributeExists("custom-title"));
  }
}
