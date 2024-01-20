package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.ElementInstanceEntity;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.rest.dto.CalledProcessInstanceDto;
import io.zeebe.monitor.rest.dto.ProcessInstanceDto;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class InstancesCalledProcessesListViewController extends AbstractInstanceViewController {

  @GetMapping("/views/instances/{key}/called-processes-list")
  @Transactional
  public String instanceDetailCalledProcessesList(
      @PathVariable("key") final long key,
      final Map<String, Object> model,
      @PageableDefault(size = DETAIL_LIST_SIZE) final Pageable pageable) {

    initializeProcessInstanceDto(key, model, pageable);
    model.put("content-called-processes-list-view", new EnableConditionalViewRenderer());
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
    final var calledProcessInstances =
        processInstanceRepository
            .findByParentProcessInstanceKey(instance.getKey(), pageable)
            .stream()
            .map(
                childEntity -> {
                  final var childDto = new CalledProcessInstanceDto();

                  childDto.setChildProcessInstanceKey(childEntity.getKey());
                  childDto.setChildBpmnProcessId(childEntity.getBpmnProcessId());
                  childDto.setChildState(childEntity.getState());

                  childDto.setElementInstanceKey(childEntity.getParentElementInstanceKey());

                  final var callElementId =
                      elementIdsForKeys.getOrDefault(childEntity.getParentElementInstanceKey(), "");
                  childDto.setElementId(callElementId);

                  return childDto;
                })
            .collect(Collectors.toList());
    dto.setCalledProcessInstances(calledProcessInstances);

    final long count = processInstanceRepository.countByParentProcessInstanceKey(instance.getKey());
    addPaginationToModel(model, pageable, count);
  }
}
