package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.ElementInstanceEntity;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.repository.ErrorRepository;
import io.zeebe.monitor.rest.dto.ProcessInstanceDto;
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
public class InstancesErrorListViewController extends AbstractInstanceViewController {

  @Autowired private ErrorRepository errorRepository;

  @GetMapping("/views/instances/{key}/error-list")
  @Transactional
  public String instanceDetailErrorList(
      @PathVariable("key") final long key,
      final Map<String, Object> model,
      @PageableDefault(size = DETAIL_LIST_SIZE) final Pageable pageable) {

    initializeProcessInstanceDto(key, model, pageable);
    model.put("content-error-list-view", new EnableConditionalViewRenderer());
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
    final var errors =
        errorRepository.findByProcessInstanceKey(instance.getKey(), pageable).stream()
            .map(ErrorsViewController::toDto)
            .collect(Collectors.toList());
    dto.setErrors(errors);

    final long count = errorRepository.countByProcessInstanceKey(instance.getKey());
    addPaginationToModel(model, pageable, count);
  }
}
