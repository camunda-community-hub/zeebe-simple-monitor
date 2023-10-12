package io.zeebe.monitor.rest;

import static io.zeebe.monitor.rest.ProcessesViewController.getBpmnElementInfos;

import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.instance.FlowElement;
import io.zeebe.monitor.entity.ElementInstanceEntity;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.rest.dto.AuditLogEntry;
import io.zeebe.monitor.rest.dto.ProcessInstanceDto;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class InstancesAuditLogViewController extends AbstractInstanceViewController {

  @GetMapping("/views/instances/{key}/audit-log")
  @Transactional
  public String instanceDetailAuditLog(
      @PathVariable final long key,
      final Map<String, Object> model,
      @PageableDefault(size = DETAIL_LIST_SIZE) final Pageable pageable) {
    initializeProcessInstanceDto(key, model, pageable);
    model.put("content-audit-log-view", new EnableConditionalViewRenderer());
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

    final var bpmnModelInstance =
        processRepository
            .findByKey(instance.getProcessDefinitionKey())
            .map(w -> new ByteArrayInputStream(w.getResource().getBytes()))
            .map(Bpmn::readModelFromStream);

    final Map<String, String> flowElements = new HashMap<>();

    bpmnModelInstance.ifPresent(
        bpmn -> {
          bpmn.getModelElementsByType(FlowElement.class)
              .forEach(
                  e -> flowElements.put(e.getId(), Optional.ofNullable(e.getName()).orElse("")));

          dto.setBpmnElementInfos(getBpmnElementInfos(bpmn));
        });

    final List<AuditLogEntry> auditLogEntries =
        events.stream()
            .skip((long) pageable.getPageSize() * pageable.getPageNumber())
            .map(
                e -> {
                  final AuditLogEntry entry = new AuditLogEntry();

                  entry.setKey(e.getKey());
                  entry.setFlowScopeKey(e.getFlowScopeKey());
                  entry.setElementId(e.getElementId());
                  entry.setElementName(flowElements.getOrDefault(e.getElementId(), ""));
                  entry.setBpmnElementType(e.getBpmnElementType().name());
                  entry.setState(e.getIntent().name());
                  entry.setTimestamp(Instant.ofEpochMilli(e.getTimestamp()).toString());

                  return entry;
                })
            .limit(pageable.getPageSize())
            .collect(Collectors.toList());
    dto.setAuditLogEntries(auditLogEntries);

    addPaginationToModel(model, pageable, events.size());
  }
}
