package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.repository.IncidentRepository;
import io.zeebe.monitor.rest.dto.IncidentListDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class IncidentsViewController extends AbstractViewController {

  @Autowired
  private IncidentRepository incidentRepository;

  @GetMapping("/views/incidents")
  @Transactional
  public String incidentList(final Map<String, Object> model, final Pageable pageable) {

    final long count = incidentRepository.countByResolvedIsNull();

    final List<IncidentListDto> incidents = new ArrayList<>();
    for (final IncidentEntity incidentEntity : incidentRepository.findByResolvedIsNull(pageable)) {
      final IncidentListDto dto = toDto(incidentEntity);
      incidents.add(dto);
    }

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
