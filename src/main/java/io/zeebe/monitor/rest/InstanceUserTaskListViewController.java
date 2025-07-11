package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.ElementInstanceEntity;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.entity.UserTaskEntity;
import io.zeebe.monitor.repository.UserTaskRepository;
import io.zeebe.monitor.rest.dto.ProcessInstanceDto;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class InstanceUserTaskListViewController extends AbstractInstanceViewController {

  @Autowired private UserTaskRepository userTaskRepository;

  @GetMapping("/views/instances/{key}/user-tasks-list")
  @Transactional
  public String UserTasksList(
      @PathVariable("key") final long key,
      final Map<String, Object> model,
      @PageableDefault(size = DETAIL_LIST_SIZE) final Pageable pageable) {

    initializeProcessInstanceDto(key, model, pageable);
    model.put("content-user-tasks-list-view", new EnableConditionalViewRenderer());
    return "instance-detail-view";
  }

  @Override
  protected void fillViewDetailsIntoDto(
      ProcessInstanceEntity instance,
      List<ElementInstanceEntity> events,
      List<IncidentEntity> incidents,
      Map<Long, String> elementIdsForKeys,
      Map<String, Object> model,
      Pageable pageable,
      ProcessInstanceDto dto) {
    final List<UserTaskEntity> userTasks =
        userTaskRepository.findByProcessInstanceKeyOrderByStartAscKeyAsc(
            instance.getKey(), pageable);
    dto.setUserTasks(userTasks);

    final long count = userTaskRepository.countByProcessInstanceKey(instance.getKey());

    addPaginationToModel(model, pageable, count);
  }
}
