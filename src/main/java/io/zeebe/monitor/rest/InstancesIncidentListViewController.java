package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.ElementInstanceEntity;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.rest.dto.IncidentDto;
import io.zeebe.monitor.rest.dto.ProcessInstanceDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class InstancesIncidentListViewController extends AbstractInstanceViewController {

  @GetMapping("/views/instances/{key}/incident-list")
  @Transactional
  public String instanceDetailIncidentList(
      @PathVariable final long key, final Map<String, Object> model, @PageableDefault(size = DETAIL_LIST_SIZE) final Pageable pageable) {

    initializeProcessInstanceDto(key, model, pageable);
    model.put("content-incident-list-view", new EnableConditionalViewRenderer());
    return "instance-detail-view";
  }

  @Override
  protected void fillViewDetailsIntoDto(
      ProcessInstanceEntity instance, List<ElementInstanceEntity> events, List<IncidentEntity> incidents, Map<Long, String> elementIdsForKeys, Map<String, Object> model, Pageable pageable, ProcessInstanceDto dto) {
    final List<IncidentDto> incidentDtos =
        incidents.stream()
            .skip((long) pageable.getPageSize() * pageable.getPageNumber())
            .map(
                i -> {
                  final long incidentKey = i.getKey();
                  final IncidentDto incidentDto = new IncidentDto();
                  incidentDto.setKey(incidentKey);
                  incidentDto.setElementId(elementIdsForKeys.get(i.getElementInstanceKey()));
                  incidentDto.setElementInstanceKey(i.getElementInstanceKey());
                  if (i.getJobKey() > 0) {
                    incidentDto.setJobKey(i.getJobKey());
                  }
                  incidentDto.setErrorType(i.getErrorType());
                  incidentDto.setErrorMessage(i.getErrorMessage());
                  final boolean isResolved = i.getResolved() != null && i.getResolved() > 0;
                  incidentDto.setResolved(isResolved);
                  incidentDto.setCreatedTime(Instant.ofEpochMilli(i.getCreated()).toString());
                  if (isResolved) {
                    incidentDto.setResolvedTime(Instant.ofEpochMilli(i.getResolved()).toString());
                    incidentDto.setState("Resolved");
                  } else {
                    incidentDto.setState("Created");
                  }
                  return incidentDto;
                })
            .limit(pageable.getPageSize())
            .collect(Collectors.toList());
    dto.setIncidents(incidentDtos);

    addPaginationToModel(model, pageable, incidents.size());
  }
}
