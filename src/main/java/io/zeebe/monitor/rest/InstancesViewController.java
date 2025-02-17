package io.zeebe.monitor.rest;

import io.micrometer.common.util.StringUtils;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.entity.ProcessInstanceState;
import io.zeebe.monitor.querydsl.ProcessInstancePredicateBuilder;
import io.zeebe.monitor.repository.ProcessInstanceBusinessKeyRepository;
import io.zeebe.monitor.repository.ProcessInstanceRepository;
import io.zeebe.monitor.rest.dto.ProcessInstanceListDto;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class InstancesViewController extends AbstractViewController {

  @Autowired protected ProcessInstanceBusinessKeyRepository processInstanceBusinessKeyRepository;
  @Autowired protected ProcessInstanceRepository processInstanceRepository;

  @GetMapping("/views/instances")
  public String instanceList(
      final Map<String, Object> model,
      final Pageable pageable,
      @RequestParam(value = "businessKey", required = false) Optional<String> businessKey,
      @RequestParam(value = "state", required = false) List<ProcessInstanceState> states) {
    final ProcessInstancePredicateBuilder predicateBuilder = new ProcessInstancePredicateBuilder();

    businessKey
        .filter(StringUtils::isNotBlank)
        .ifPresent(
            it -> {
              var instances =
                  processInstanceBusinessKeyRepository.findAllByBusinessKeyStartsWith(it);
              predicateBuilder.withKeys(instances);
            });

    predicateBuilder.withStates(states);

    var instancesEntity = processInstanceRepository.findAll(predicateBuilder.build(), pageable);
    final List<ProcessInstanceListDto> instances = new ArrayList<>();
    for (final ProcessInstanceEntity instanceEntity : instancesEntity) {
      final ProcessInstanceListDto dto = ProcessesViewController.toDto(instanceEntity);
      instances.add(dto);
    }

    final long count = instancesEntity.getTotalElements();

    model.put("instances", instances);
    model.put("count", count);
    model.put("businessKey", businessKey.orElse(""));
    model.put("state", buildStateFilters(states));

    addPaginationToModel(model, pageable, count);
    addDefaultAttributesToModel(model);

    return "instance-list-view";
  }

  private List<ProcessInstanceStateFilter> buildStateFilters(
      List<ProcessInstanceState> selectedStates) {
    return Arrays.stream(ProcessInstanceState.values())
        .map(
            it ->
                new ProcessInstanceStateFilter(
                    it, Objects.nonNull(selectedStates) && selectedStates.contains(it)))
        .toList();
  }

  public static class ProcessInstanceStateFilter {
    private String value;
    private boolean selected;

    public ProcessInstanceStateFilter(ProcessInstanceState value, boolean selected) {
      this.value = value.name();
      this.selected = selected;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public boolean isSelected() {
      return selected;
    }

    public void setSelected(boolean selected) {
      this.selected = selected;
    }
  }
}
