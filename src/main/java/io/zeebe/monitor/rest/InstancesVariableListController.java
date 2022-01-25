package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.ElementInstanceEntity;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.entity.VariableEntity;
import io.zeebe.monitor.repository.VariableRepository;
import io.zeebe.monitor.rest.dto.ProcessInstanceDto;
import io.zeebe.monitor.rest.dto.VariableEntry;
import io.zeebe.monitor.rest.dto.VariableUpdateEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class InstancesVariableListController extends AbstractInstanceViewController {

  @Autowired
  private VariableRepository variableRepository;

  @GetMapping("/views/instances/{key}")
  @Transactional
  public String instanceVariableList(
      @PathVariable final long key, final Map<String, Object> model, @PageableDefault(size = DETAIL_LIST_SIZE) final Pageable pageable) {
    return instanceDetailVariableList(key, model, pageable);
  }

  @GetMapping("/views/instances/{key}/variable-list")
  @Transactional
  public String instanceDetailVariableList(
      @PathVariable final long key, final Map<String, Object> model, final Pageable pageable) {

    initializeProcessInstanceDto(key, model, pageable);
    model.put("content-variable-list-view", new EnableConditionalViewRenderer());
    return "instance-detail-view";
  }

  @Override
  protected void fillViewDetailsIntoDto(ProcessInstanceEntity instance, List<ElementInstanceEntity> events, List<IncidentEntity> incidents, Map<Long, String> elementIdsForKeys, Map<String, Object> model, Pageable pageable, ProcessInstanceDto dto) {
    final Map<VariableTuple, List<VariableEntity>> variablesByScopeAndName =
        variableRepository.findByProcessInstanceKey(instance.getKey(), pageable).stream()
            .collect(Collectors.groupingBy(v -> new VariableTuple(v.getScopeKey(), v.getName())));
    variablesByScopeAndName.forEach(
        (scopeKeyName, variables) -> {
          final VariableEntry variableDto = new VariableEntry();
          final long scopeKey = scopeKeyName.scopeKey;

          variableDto.setScopeKey(scopeKey);
          variableDto.setElementId(elementIdsForKeys.get(scopeKey));

          variableDto.setName(scopeKeyName.name);

          final VariableEntity lastUpdate = variables.get(variables.size() - 1);
          variableDto.setValue(lastUpdate.getValue());
          variableDto.setTimestamp(Instant.ofEpochMilli(lastUpdate.getTimestamp()).toString());

          final List<VariableUpdateEntry> varUpdates =
              variables.stream()
                  .map(
                      v -> {
                        final VariableUpdateEntry varUpdate = new VariableUpdateEntry();
                        varUpdate.setValue(v.getValue());
                        varUpdate.setTimestamp(Instant.ofEpochMilli(v.getTimestamp()).toString());
                        return varUpdate;
                      })
                  .collect(Collectors.toList());
          variableDto.setUpdates(varUpdates);

          dto.getVariables().add(variableDto);
        });

    final long count = variableRepository.countByProcessInstanceKey(instance.getKey());
    addPaginationToModel(model, pageable, count);
  }

  private static class VariableTuple {
    private final long scopeKey;
    private final String name;

    VariableTuple(final long scopeKey, final String name) {
      this.scopeKey = scopeKey;
      this.name = name;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      final VariableTuple that = (VariableTuple) o;
      return scopeKey == that.scopeKey && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(scopeKey, name);
    }
  }
}
