package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.repository.ProcessInstanceRepository;
import io.zeebe.monitor.rest.dto.ProcessInstanceListDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InstancesViewController extends AbstractViewController {

  @Autowired protected ProcessInstanceRepository processInstanceRepository;

  @GetMapping("/views/instances")
  public String instanceList(final Map<String, Object> model, final Pageable pageable) {

    final long count = processInstanceRepository.count();

    final List<ProcessInstanceListDto> instances = new ArrayList<>();
    for (final ProcessInstanceEntity instanceEntity : processInstanceRepository.findAll(pageable)) {
      final ProcessInstanceListDto dto = ProcessesViewController.toDto(instanceEntity);
      instances.add(dto);
    }

    model.put("instances", instances);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);
    addDefaultAttributesToModel(model);

    return "instance-list-view";
  }
}
