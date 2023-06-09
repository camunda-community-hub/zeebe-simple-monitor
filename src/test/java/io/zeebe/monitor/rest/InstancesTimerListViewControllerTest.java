package io.zeebe.monitor.rest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.zeebe.monitor.entity.ProcessEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

class InstancesTimerListViewControllerTest extends AbstractViewOrResourceTest {

  @Autowired protected InstancesTimerListViewController instancesTimerListViewController;

  @BeforeEach
  public void setUp() {
    when(processRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
    when(elementInstanceRepository.findByProcessInstanceKey(anyLong())).thenReturn(Page.empty());
    when(timerRepository.findByProcessInstanceKey(anyLong(), any(Pageable.class)))
        .thenReturn(Page.empty());
  }

  @Test
  void timers_list_view_contains_pagination_elements() throws Exception {
    // GIVEN
    BpmnModelInstance modelInstance = Bpmn.readModelFromStream(this.getClass().getClassLoader().getResourceAsStream("orderProcess.bpmn"));
    ProcessInstanceEntity processInstanceEntity = mock(ProcessInstanceEntity.class, RETURNS_MOCKS);
    ProcessEntity processEntity = mock(ProcessEntity.class, RETURNS_MOCKS);
    when(processEntity.getResource()).thenReturn(modelInstance.toString());

    when(processInstanceRepository.findByKey(anyLong()))
        .thenReturn(Optional.of(processInstanceEntity));
    when(processRepository.findByKey(anyLong())).thenReturn(Optional.of(processEntity));

    // WHEN
    mockMvc
        .perform(get("/views/instances/12345678/timer-list"))
        // THEN
        .andExpect(
            content()
                .string(
                    containsString(
                        "<a class=\"page-link\" href=\"#\" tabindex=\"-1\">Previous</a>")));
  }
}
