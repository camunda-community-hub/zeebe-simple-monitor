package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.ActivityInstanceEntity;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.entity.WorkflowEntity;
import io.zeebe.monitor.entity.WorkflowInstanceEntity;
import io.zeebe.monitor.repository.ActivityInstanceRepository;
import io.zeebe.monitor.repository.IncidentRepository;
import io.zeebe.monitor.repository.WorkflowInstanceRepository;
import io.zeebe.monitor.repository.WorkflowRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

  private static final List<String> WORKFLOW_INSTANCE_ENTERED_INTENTS =
      Arrays.asList(
          "ELEMENT_ACTIVATED", "START_EVENT_OCCURRED", "END_EVENT_OCCURRED", "GATEWAY_ACTIVATED");

  private static final List<String> WORKFLOW_INSTANCE_COMPLETED_INTENTS =
      Arrays.asList(
          "ELEMENT_COMPLETED",
          "ELEMENT_TERMINATED",
          "START_EVENT_OCCURRED",
          "END_EVENT_OCCURRED",
          "GATEWAY_ACTIVATED");

  @Autowired private WorkflowRepository workflowRepository;

  @Autowired private WorkflowInstanceRepository workflowInstanceRepository;

  @Autowired private ActivityInstanceRepository activityInstanceRepository;

  @Autowired private IncidentRepository incidentRepository;

  @GetMapping("/")
  public String index(Map<String, Object> model, Pageable pageable) {
    return workflowList(model, pageable);
  }

  @GetMapping("/views/workflows")
  public String workflowList(Map<String, Object> model, Pageable pageable) {

    final long count = workflowRepository.count();

    final List<WorkflowDto> workflows = new ArrayList<>();
    for (WorkflowEntity workflowEntity : workflowRepository.findAll(pageable)) {
      final WorkflowDto dto = toDto(workflowEntity);
      workflows.add(dto);
    }

    model.put("workflows", workflows);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);

    return "workflow-list-view";
  }

  private WorkflowDto toDto(WorkflowEntity workflowEntity) {
    final long workflowKey = workflowEntity.getKey();

    final long running = workflowInstanceRepository.countByWorkflowKeyAndEndIsNull(workflowKey);
    final long ended = workflowInstanceRepository.countByWorkflowKeyAndEndIsNotNull(workflowKey);

    final WorkflowDto dto = WorkflowDto.from(workflowEntity, running, ended);
    return dto;
  }

  @GetMapping("/views/workflows/{key}")
  public String workflowDetail(
      @PathVariable long key, Map<String, Object> model, Pageable pageable) {

    workflowRepository
        .findByKey(key)
        .ifPresent(
            workflow -> {
              model.put("workflow", toDto(workflow));
              model.put("resource", workflow.getResource());
            });

    final long count = workflowInstanceRepository.countByWorkflowKey(key);

    final List<WorkflowInstanceListDto> instances = new ArrayList<>();
    for (WorkflowInstanceEntity instanceEntity :
        workflowInstanceRepository.findByWorkflowKey(key, pageable)) {
      instances.add(toDto(instanceEntity));
    }

    model.put("instances", instances);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);

    return "workflow-detail-view";
  }

  private WorkflowInstanceListDto toDto(WorkflowInstanceEntity instance) {

    final WorkflowInstanceListDto dto = new WorkflowInstanceListDto();
    dto.setWorkflowInstanceKey(instance.getKey());

    dto.setBpmnProcessId(instance.getBpmnProcessId());
    dto.setWorkflowKey(instance.getWorkflowKey());

    final boolean isEnded = instance.getEnd() != null && instance.getEnd() > 0;
    dto.setState(instance.getState());

    dto.setStartTime(Instant.ofEpochMilli(instance.getStart()).toString());

    if (isEnded) {
      dto.setEndTime(Instant.ofEpochMilli(instance.getEnd()).toString());
    }

    return dto;
  }

  @GetMapping("/views/instances")
  public String instanceList(Map<String, Object> model, Pageable pageable) {

    final long count = workflowInstanceRepository.count();

    final List<WorkflowInstanceListDto> instances = new ArrayList<>();
    for (WorkflowInstanceEntity instanceEntity : workflowInstanceRepository.findAll(pageable)) {
      final WorkflowInstanceListDto dto = toDto(instanceEntity);
      instances.add(dto);
    }

    model.put("instances", instances);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);

    return "instance-list-view";
  }

  @GetMapping("/views/instances/{key}")
  public String instanceDetail(
      @PathVariable long key, Map<String, Object> model, Pageable pageable) {

    workflowInstanceRepository
        .findByKey(key)
        .ifPresent(
            instance -> {
              workflowRepository
                  .findByKey(instance.getWorkflowKey())
                  .ifPresent(workflow -> model.put("resource", workflow.getResource()));

              model.put("instance", toInstanceDto(instance));
            });

    return "instance-detail-view";
  }

  private WorkflowInstanceDto toInstanceDto(WorkflowInstanceEntity instance) {
    final List<ActivityInstanceEntity> events =
        StreamSupport.stream(
                activityInstanceRepository
                    .findByWorkflowInstanceKey(instance.getKey())
                    .spliterator(),
                false)
            .collect(Collectors.toList());

    final ActivityInstanceEntity lastEvent = events.get(events.size() - 1);

    final WorkflowInstanceDto dto = new WorkflowInstanceDto();
    dto.setWorkflowInstanceKey(instance.getKey());

    dto.setPartitionId(instance.getPartitionId());

    dto.setWorkflowKey(instance.getWorkflowKey());
    dto.setPayload(lastEvent.getPayload());

    dto.setBpmnProcessId(instance.getBpmnProcessId());
    dto.setVersion(instance.getVersion());

    final boolean isEnded = instance.getEnd() != null && instance.getEnd() > 0;
    dto.setState(instance.getState());
    dto.setRunning(!isEnded);

    dto.setStartTime(Instant.ofEpochMilli(instance.getStart()).toString());

    if (isEnded) {
      dto.setEndTime(Instant.ofEpochMilli(instance.getEnd()).toString());
    }

    final List<String> completedActivities =
        events
            .stream()
            .filter(e -> WORKFLOW_INSTANCE_COMPLETED_INTENTS.contains(e.getIntent()))
            .map(ActivityInstanceEntity::getActivityId)
            .collect(Collectors.toList());

    final List<String> activeActivities =
        events
            .stream()
            .filter(e -> WORKFLOW_INSTANCE_ENTERED_INTENTS.contains(e.getIntent()))
            .map(ActivityInstanceEntity::getActivityId)
            .filter(id -> !completedActivities.contains(id))
            .collect(Collectors.toList());
    dto.setActiveActivities(activeActivities);

    final List<String> takenSequenceFlows =
        events
            .stream()
            .filter(e -> e.getIntent().equals("SEQUENCE_FLOW_TAKEN"))
            .map(ActivityInstanceEntity::getActivityId)
            .collect(Collectors.toList());
    dto.setTakenSequenceFlows(takenSequenceFlows);

    final Map<String, Long> completedElementsById =
        events
            .stream()
            .filter(e -> WORKFLOW_INSTANCE_COMPLETED_INTENTS.contains(e.getIntent()))
            .collect(
                Collectors.groupingBy(
                    ActivityInstanceEntity::getActivityId, Collectors.counting()));

    final Map<String, Long> enteredElementsById =
        events
            .stream()
            .filter(e -> WORKFLOW_INSTANCE_ENTERED_INTENTS.contains(e.getIntent()))
            .collect(
                Collectors.groupingBy(
                    ActivityInstanceEntity::getActivityId, Collectors.counting()));

    final List<ElementInstanceState> elementStates =
        enteredElementsById
            .entrySet()
            .stream()
            .map(
                e -> {
                  final String elementId = e.getKey();

                  final long enteredInstances = e.getValue();
                  final long completedInstances = completedElementsById.getOrDefault(elementId, 0L);

                  final ElementInstanceState state = new ElementInstanceState();
                  state.setElementId(elementId);
                  state.setActiveInstances(enteredInstances - completedInstances);
                  state.setEndedInstances(completedInstances);

                  return state;
                })
            .collect(Collectors.toList());

    dto.setElementInstances(elementStates);

    final List<AuditLogEntry> auditLogEntries =
        events
            .stream()
            .map(
                e -> {
                  final AuditLogEntry entry = new AuditLogEntry();

                  entry.setKey(e.getKey());
                  entry.setScopeInstanceKey(e.getScopeInstanceKey());
                  entry.setElementId(e.getActivityId());
                  entry.setPaylaod(e.getPayload());
                  entry.setState(e.getIntent());
                  entry.setTimestamp(Instant.ofEpochMilli(e.getTimestamp()).toString());

                  return entry;
                })
            .collect(Collectors.toList());

    dto.setAuditLogEntries(auditLogEntries);

    final List<IncidentEntity> incidents =
        StreamSupport.stream(
                incidentRepository.findByWorkflowInstanceKey(instance.getKey()).spliterator(),
                false)
            .collect(Collectors.toList());

    final Map<Long, String> elementIdsForKeys = new HashMap<>();
    events.forEach(e -> elementIdsForKeys.put(e.getKey(), e.getActivityId()));

    final List<IncidentDto> incidentDtos =
        incidents
            .stream()
            .map(
                i -> {
                  final Long incidentKey = i.getIncidentKey();

                  final IncidentDto incidentDto = new IncidentDto();
                  incidentDto.setKey(incidentKey);

                  incidentDto.setActivityId(elementIdsForKeys.get(i.getActivityInstanceKey()));
                  incidentDto.setActivityInstanceKey(i.getActivityInstanceKey());

                  events
                      .stream()
                      .filter(e -> e.getKey() == i.getActivityInstanceKey())
                      .findFirst()
                      .ifPresent(e -> incidentDto.setPayload(e.getPayload()));

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
            .collect(Collectors.toList());
    dto.setIncidents(incidentDtos);

    final List<String> activitiesWitIncidents =
        incidents
            .stream()
            .filter(i -> i.getResolved() == null || i.getResolved() <= 0)
            .map(i -> elementIdsForKeys.get(i.getActivityInstanceKey()))
            .distinct()
            .collect(Collectors.toList());

    dto.setIncidentActivities(activitiesWitIncidents);

    activeActivities.removeAll(activitiesWitIncidents);
    dto.setActiveActivities(activeActivities);

    return dto;
  }

  @GetMapping("/views/incidents")
  public String incidentList(Map<String, Object> model, Pageable pageable) {

    final long count = incidentRepository.countByResolvedIsNull();

    final List<IncidentListDto> incidents = new ArrayList<>();
    for (IncidentEntity incidentEntity : incidentRepository.findByResolvedIsNull(pageable)) {
      final IncidentListDto dto = toDto(incidentEntity);
      incidents.add(dto);
    }

    model.put("incidents", incidents);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);

    return "incident-list-view";
  }

  private IncidentListDto toDto(IncidentEntity incident) {
    final IncidentListDto dto = new IncidentListDto();
    dto.setKey(incident.getIncidentKey());

    dto.setWorkflowInstanceKey(incident.getWorkflowInstanceKey());

    workflowInstanceRepository
        .findByKey(incident.getWorkflowInstanceKey())
        .ifPresent(
            instance -> {
              dto.setBpmnProcessId(instance.getBpmnProcessId());
              dto.setWorkflowKey(instance.getWorkflowKey());
            });

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

  private void addPaginationToModel(
      Map<String, Object> model, Pageable pageable, final long count) {

    final int currentPage = pageable.getPageNumber();
    model.put("page", currentPage + 1);
    if (currentPage > 0) {
      model.put("prevPage", currentPage - 1);
    }
    if (count > (1 + currentPage) * pageable.getPageSize()) {
      model.put("nextPage", currentPage + 1);
    }
  }
}
