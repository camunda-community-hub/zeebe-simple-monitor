package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.ElementInstanceEntity;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.repository.TimerRepository;
import io.zeebe.monitor.rest.dto.ProcessInstanceDto;
import io.zeebe.monitor.rest.dto.TimerDto;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class InstancesTimerListViewController extends AbstractInstanceViewController {

  @Autowired private TimerRepository timerRepository;

  @GetMapping("/views/instances/{key}/timer-list")
  @Transactional
  public String instanceDetailTimerList(
      @PathVariable("key") final long key,
      final Map<String, Object> model,
      @PageableDefault(size = DETAIL_LIST_SIZE) final Pageable pageable) {

    initializeProcessInstanceDto(key, model, pageable);
    model.put("content-timer-list-view", new EnableConditionalViewRenderer());
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
    final List<TimerDto> timers =
        timerRepository.findByProcessInstanceKey(instance.getKey(), pageable).stream()
            .map(ProcessesViewController::toDto)
            .collect(Collectors.toList());
    dto.setTimers(timers);

    final long count = timerRepository.countByProcessInstanceKey(instance.getKey());
    addPaginationToModel(model, pageable, count);
  }
}
