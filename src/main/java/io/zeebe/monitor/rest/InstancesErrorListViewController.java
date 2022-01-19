package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.ElementInstanceEntity;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.rest.dto.ProcessInstanceDto;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class InstancesErrorListViewController extends AbstractInstanceViewController {

  @GetMapping("/views/instances/{key}/error-list")
  @Transactional
  public String instanceDetailErrorList(
      @PathVariable final long key, final Map<String, Object> model, final Pageable pageable) {

    initializeProcessInstanceDto(key, model, pageable);
    model.put("content-error-list-view", new EnableConditionalViewRenderer());
    return "instance-detail-view";
  }

  @Override
  protected void fillViewDetailsIntoDto(ProcessInstanceEntity instance, List<ElementInstanceEntity> events, List<IncidentEntity> incidents, Map<Long, String> elementIdsForKeys, Map<String, Object> model, Pageable pageable, ProcessInstanceDto dto) {
    final var errors =
        errorRepository.findByProcessInstanceKey(instance.getKey()).stream()
            .map(ErrorsViewController::toDto)
            .collect(Collectors.toList());
    dto.setErrors(errors);
  }
}
