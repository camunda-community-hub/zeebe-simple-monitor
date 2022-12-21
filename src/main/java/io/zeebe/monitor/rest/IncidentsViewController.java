package io.zeebe.monitor.rest;

import com.querydsl.core.types.Predicate;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.querydsl.IncidentEntityPredicatesBuilder;
import io.zeebe.monitor.repository.IncidentRepository;
import io.zeebe.monitor.rest.dto.IncidentListDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class IncidentsViewController extends AbstractViewController {

  @Autowired private IncidentRepository incidentRepository;

  @GetMapping("/views/incidents")
  @Transactional
  public String incidentList(final Map<String, Object> model,
                             final Pageable pageable,
                             @RequestParam(required = false) String bpmnProcessId,
                             @RequestParam(required = false) String errorType,
                             @RequestParam(required = false) String createdAfter,
                             @RequestParam(required = false) String createdBefore) {

    final Predicate predicate = new IncidentEntityPredicatesBuilder()
        .onlyUnresolved()
        .withProcessId(bpmnProcessId)
        .withErrorType(errorType)
        .createdAfter(createdAfter)
        .createdBefore(createdBefore)
        .build();

    final Page<IncidentEntity> dtos = incidentRepository.findAll(predicate, pageable);
    final List<IncidentListDto> incidents = new ArrayList<>();
    for (final IncidentEntity incidentEntity : dtos) {
      final IncidentListDto dto = toDto(incidentEntity);
      incidents.add(dto);
    }

    final long count = dtos.getTotalElements();

    model.put("incidents", incidents);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);
    addDefaultAttributesToModel(model);

    return "incident-list-view";
  }

  private IncidentListDto toDto(final IncidentEntity incident) {
    final IncidentListDto dto = new IncidentListDto();
    dto.setKey(incident.getKey());

    dto.setBpmnProcessId(incident.getBpmnProcessId());
    dto.setProcessDefinitionKey(incident.getProcessDefinitionKey());
    dto.setProcessInstanceKey(incident.getProcessInstanceKey());

    dto.setErrorType(incident.getErrorType());
    dto.setErrorMessage(incident.getErrorMessage());

    final boolean isResolved = incident.getResolved() != null && incident.getResolved() > 0;

    dto.setCreatedTime(Instant.ofEpochMilli(incident.getCreated()).toString());

    if (isResolved) {
      dto.setResolvedTime(Instant.ofEpochMilli(incident.getResolved()).toString());

      dto.setState("Resolved");
    } else {
      dto.setState("Created");
    }

    return dto;
  }
}
