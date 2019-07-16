package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.ElementInstanceEntity;
import io.zeebe.monitor.entity.ElementInstanceStatistics;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.entity.JobEntity;
import io.zeebe.monitor.entity.MessageEntity;
import io.zeebe.monitor.entity.MessageSubscriptionEntity;
import io.zeebe.monitor.entity.TimerEntity;
import io.zeebe.monitor.entity.VariableEntity;
import io.zeebe.monitor.entity.WorkflowEntity;
import io.zeebe.monitor.entity.WorkflowInstanceEntity;
import io.zeebe.monitor.repository.ElementInstanceRepository;
import io.zeebe.monitor.repository.IncidentRepository;
import io.zeebe.monitor.repository.JobRepository;
import io.zeebe.monitor.repository.MessageRepository;
import io.zeebe.monitor.repository.MessageSubscriptionRepository;
import io.zeebe.monitor.repository.TimerRepository;
import io.zeebe.monitor.repository.VariableRepository;
import io.zeebe.monitor.repository.WorkflowInstanceRepository;
import io.zeebe.monitor.repository.WorkflowRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
      Arrays.asList("ELEMENT_ACTIVATED");

  private static final List<String> WORKFLOW_INSTANCE_COMPLETED_INTENTS =
      Arrays.asList("ELEMENT_COMPLETED", "ELEMENT_TERMINATED");

  private static final List<String> JOB_COMPLETED_INTENTS = Arrays.asList("completed", "canceled");

  @Autowired private WorkflowRepository workflowRepository;

  @Autowired private WorkflowInstanceRepository workflowInstanceRepository;

  @Autowired private ElementInstanceRepository activityInstanceRepository;

  @Autowired private IncidentRepository incidentRepository;

  @Autowired private JobRepository jobRepository;

  @Autowired private MessageRepository messageRepository;

  @Autowired private MessageSubscriptionRepository messageSubscriptionRepository;

  @Autowired private TimerRepository timerRepository;

  @Autowired private VariableRepository variableRepository;

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

    final WorkflowEntity workflow =
        workflowRepository
            .findByKey(key)
            .orElseThrow(() -> new RuntimeException("No workflow found with key: " + key));

    model.put("workflow", toDto(workflow));
    model.put("resource", workflow.getResource());

    final List<ElementInstanceState> elementInstanceStates = getElementInstanceStates(key);
    model.put("instance.elementInstances", elementInstanceStates);

    final long count = workflowInstanceRepository.countByWorkflowKey(key);

    final List<WorkflowInstanceListDto> instances = new ArrayList<>();
    for (WorkflowInstanceEntity instanceEntity :
        workflowInstanceRepository.findByWorkflowKey(key, pageable)) {
      instances.add(toDto(instanceEntity));
    }

    model.put("instances", instances);
    model.put("count", count);

    final List<TimerDto> timers =
        timerRepository.findByWorkflowKeyAndWorkflowInstanceKeyIsNull(key).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    model.put("timers", timers);

    final List<MessageSubscriptionDto> messageSubscriptions =
        messageSubscriptionRepository.findByWorkflowKeyAndWorkflowInstanceKeyIsNull(key).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    model.put("messageSubscriptions", messageSubscriptions);

    addPaginationToModel(model, pageable, count);

    return "workflow-detail-view";
  }

  private List<ElementInstanceState> getElementInstanceStates(long key) {

    final List<ElementInstanceStatistics> elementEnteredStatistics =
        workflowRepository.getElementInstanceStatisticsByKeyAndIntentIn(
            key, WORKFLOW_INSTANCE_ENTERED_INTENTS);

    final Map<String, Long> elementCompletedCount =
        workflowRepository
            .getElementInstanceStatisticsByKeyAndIntentIn(key, WORKFLOW_INSTANCE_COMPLETED_INTENTS)
            .stream()
            .collect(
                Collectors.toMap(
                    ElementInstanceStatistics::getElementId, ElementInstanceStatistics::getCount));

    final List<ElementInstanceState> elementInstanceStates =
        elementEnteredStatistics.stream()
            .map(
                s -> {
                  final ElementInstanceState state = new ElementInstanceState();

                  final String elementId = s.getElementId();
                  state.setElementId(elementId);

                  final long completedInstances = elementCompletedCount.getOrDefault(elementId, 0L);
                  long enteredInstances = s.getCount();

                  state.setActiveInstances(enteredInstances - completedInstances);
                  state.setEndedInstances(completedInstances);

                  return state;
                })
            .collect(Collectors.toList());
    return elementInstanceStates;
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

    final WorkflowInstanceEntity instance =
        workflowInstanceRepository
            .findByKey(key)
            .orElseThrow(() -> new RuntimeException("No workflow instance found with key: " + key));

    workflowRepository
        .findByKey(instance.getWorkflowKey())
        .ifPresent(workflow -> model.put("resource", workflow.getResource()));

    model.put("instance", toInstanceDto(instance));

    return "instance-detail-view";
  }

  private WorkflowInstanceDto toInstanceDto(WorkflowInstanceEntity instance) {
    final List<ElementInstanceEntity> events =
        StreamSupport.stream(
                activityInstanceRepository
                    .findByWorkflowInstanceKey(instance.getKey())
                    .spliterator(),
                false)
            .collect(Collectors.toList());

    final WorkflowInstanceDto dto = new WorkflowInstanceDto();
    dto.setWorkflowInstanceKey(instance.getKey());

    dto.setPartitionId(instance.getPartitionId());

    dto.setWorkflowKey(instance.getWorkflowKey());

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
        events.stream()
            .filter(e -> WORKFLOW_INSTANCE_COMPLETED_INTENTS.contains(e.getIntent()))
            .map(ElementInstanceEntity::getElementId)
            .collect(Collectors.toList());

    final List<String> activeActivities =
        events.stream()
            .filter(e -> WORKFLOW_INSTANCE_ENTERED_INTENTS.contains(e.getIntent()))
            .map(ElementInstanceEntity::getElementId)
            .filter(id -> !completedActivities.contains(id))
            .collect(Collectors.toList());
    dto.setActiveActivities(activeActivities);

    final List<String> takenSequenceFlows =
        events.stream()
            .filter(e -> e.getIntent().equals("SEQUENCE_FLOW_TAKEN"))
            .map(ElementInstanceEntity::getElementId)
            .collect(Collectors.toList());
    dto.setTakenSequenceFlows(takenSequenceFlows);

    final Map<String, Long> completedElementsById =
        events.stream()
            .filter(e -> WORKFLOW_INSTANCE_COMPLETED_INTENTS.contains(e.getIntent()))
            .collect(
                Collectors.groupingBy(ElementInstanceEntity::getElementId, Collectors.counting()));

    final Map<String, Long> enteredElementsById =
        events.stream()
            .filter(e -> WORKFLOW_INSTANCE_ENTERED_INTENTS.contains(e.getIntent()))
            .collect(
                Collectors.groupingBy(ElementInstanceEntity::getElementId, Collectors.counting()));

    final List<ElementInstanceState> elementStates =
        enteredElementsById.entrySet().stream()
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
        events.stream()
            .map(
                e -> {
                  final AuditLogEntry entry = new AuditLogEntry();

                  entry.setKey(e.getKey());
                  entry.setFlowScopeKey(e.getFlowScopeKey());
                  entry.setElementId(e.getElementId());
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
    elementIdsForKeys.put(instance.getKey(), instance.getBpmnProcessId());
    events.forEach(e -> elementIdsForKeys.put(e.getKey(), e.getElementId()));

    final List<IncidentDto> incidentDtos =
        incidents.stream()
            .map(
                i -> {
                  final long incidentKey = i.getKey();

                  final IncidentDto incidentDto = new IncidentDto();
                  incidentDto.setKey(incidentKey);

                  incidentDto.setActivityId(elementIdsForKeys.get(i.getElementInstanceKey()));
                  incidentDto.setActivityInstanceKey(i.getElementInstanceKey());

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
        incidents.stream()
            .filter(i -> i.getResolved() == null || i.getResolved() <= 0)
            .map(i -> elementIdsForKeys.get(i.getElementInstanceKey()))
            .distinct()
            .collect(Collectors.toList());

    dto.setIncidentActivities(activitiesWitIncidents);

    activeActivities.removeAll(activitiesWitIncidents);
    dto.setActiveActivities(activeActivities);

    final Map<VariableTuple, List<VariableEntity>> variablesByScopeAndName =
        variableRepository.findByWorkflowInstanceKey(instance.getKey()).stream()
            .collect(Collectors.groupingBy(v -> new VariableTuple(v.getScopeKey(), v.getName())));
    variablesByScopeAndName.forEach(
        (scopeKeyName, variables) -> {
          final VariableEntry variableDto = new VariableEntry();
          final long scopeKey = scopeKeyName.scopeKey;

          variableDto.setScopeKey(scopeKey);
          variableDto.setScopeName(elementIdsForKeys.get(scopeKey));

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

    final List<ActiveScope> activeScopes = new ArrayList<>();
    if (!isEnded) {
      activeScopes.add(new ActiveScope(instance.getKey(), instance.getBpmnProcessId()));

      final List<Long> completedElementInstances =
          events.stream()
              .filter(e -> WORKFLOW_INSTANCE_COMPLETED_INTENTS.contains(e.getIntent()))
              .map(ElementInstanceEntity::getKey)
              .collect(Collectors.toList());

      final List<ActiveScope> activeElementInstances =
          events.stream()
              .filter(e -> WORKFLOW_INSTANCE_ENTERED_INTENTS.contains(e.getIntent()))
              .map(ElementInstanceEntity::getKey)
              .filter(id -> !completedElementInstances.contains(id))
              .map(scopeKey -> new ActiveScope(scopeKey, elementIdsForKeys.get(scopeKey)))
              .collect(Collectors.toList());

      activeScopes.addAll(activeElementInstances);
    }
    dto.setActiveScopes(activeScopes);

    final List<JobDto> jobDtos =
        jobRepository.findByWorkflowInstanceKey(instance.getKey()).stream()
            .map(
                job -> {
                  final JobDto jobDto = toDto(job);
                  jobDto.setActivityId(
                      elementIdsForKeys.getOrDefault(job.getElementInstanceKey(), ""));

                  final boolean isActivatable =
                      job.getRetries() > 0
                          && Arrays.asList("created", "failed", "timed_out", "retries_updated")
                              .contains(job.getState());
                  jobDto.setActivatable(isActivatable);

                  return jobDto;
                })
            .collect(Collectors.toList());
    dto.setJobs(jobDtos);

    final List<MessageSubscriptionDto> messageSubscriptions =
        messageSubscriptionRepository.findByWorkflowInstanceKey(instance.getKey()).stream()
            .map(
                subscription -> {
                  final MessageSubscriptionDto subscriptionDto = toDto(subscription);
                  subscriptionDto.setActivityId(
                      elementIdsForKeys.getOrDefault(subscriptionDto.getActivityInstanceKey(), ""));

                  return subscriptionDto;
                })
            .collect(Collectors.toList());
    dto.setMessageSubscriptions(messageSubscriptions);

    final List<TimerDto> timers =
        timerRepository.findByWorkflowInstanceKey(instance.getKey()).stream()
            .map(timer -> toDto(timer))
            .collect(Collectors.toList());
    dto.setTimers(timers);

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
    dto.setKey(incident.getKey());

    dto.setBpmnProcessId(incident.getBpmnProcessId());
    dto.setWorkflowKey(incident.getWorkflowKey());
    ;
    dto.setWorkflowInstanceKey(incident.getWorkflowInstanceKey());

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

  @GetMapping("/views/jobs")
  public String jobList(Map<String, Object> model, Pageable pageable) {

    final long count = jobRepository.countByStateNotIn(JOB_COMPLETED_INTENTS);

    final List<JobDto> dtos = new ArrayList<>();
    for (JobEntity jobEntity : jobRepository.findByStateNotIn(JOB_COMPLETED_INTENTS, pageable)) {
      final JobDto dto = toDto(jobEntity);
      dtos.add(dto);
    }

    model.put("jobs", dtos);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);

    return "job-list-view";
  }

  private JobDto toDto(JobEntity job) {
    final JobDto dto = new JobDto();

    dto.setKey(job.getKey());
    dto.setJobType(job.getJobType());
    dto.setWorkflowInstanceKey(job.getWorkflowInstanceKey());
    dto.setActivityInstanceKey(job.getElementInstanceKey());
    dto.setState(job.getState());
    dto.setRetries(job.getRetries());
    Optional.ofNullable(job.getWorker()).ifPresent(dto::setWorker);
    dto.setTimestamp(Instant.ofEpochMilli(job.getTimestamp()).toString());

    return dto;
  }

  @GetMapping("/views/messages")
  public String messageList(Map<String, Object> model, Pageable pageable) {

    final long count = messageRepository.count();

    final List<MessageDto> dtos = new ArrayList<>();
    for (MessageEntity messageEntity : messageRepository.findAll(pageable)) {
      final MessageDto dto = toDto(messageEntity);
      dtos.add(dto);
    }

    model.put("messages", dtos);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);

    return "message-list-view";
  }

  private MessageDto toDto(MessageEntity message) {
    final MessageDto dto = new MessageDto();

    dto.setKey(message.getKey());
    dto.setName(message.getName());
    dto.setCorrelationKey(message.getCorrelationKey());
    dto.setMessageId(message.getMessageId());
    dto.setPayload(message.getPayload());
    dto.setState(message.getState());
    dto.setTimestamp(Instant.ofEpochMilli(message.getTimestamp()).toString());

    return dto;
  }

  private MessageSubscriptionDto toDto(MessageSubscriptionEntity subscription) {
    final MessageSubscriptionDto dto = new MessageSubscriptionDto();

    dto.setMessageName(subscription.getMessageName());
    dto.setCorrelationKey(subscription.getCorrelationKey());

    dto.setWorkflowInstanceKey(subscription.getWorkflowInstanceKey());
    dto.setActivityInstanceKey(subscription.getElementInstanceKey());

    dto.setActivityId(subscription.getTargetFlowNodeId());

    dto.setState(subscription.getState());
    dto.setTimestamp(Instant.ofEpochMilli(subscription.getTimestamp()).toString());

    dto.setOpen(subscription.getState().equals("opened"));

    return dto;
  }

  private TimerDto toDto(TimerEntity timer) {
    final TimerDto dto = new TimerDto();

    dto.setActivityId(timer.getTargetFlowNodeId());
    dto.setState(timer.getState());
    dto.setDueDate(Instant.ofEpochMilli(timer.getDueDate()).toString());
    dto.setTimestamp(Instant.ofEpochMilli(timer.getTimestamp()).toString());
    dto.setActivityInstanceKey(timer.getElementInstanceKey());

    final int repetitions = timer.getRepetitions();
    dto.setRepetitions(repetitions >= 0 ? String.valueOf(repetitions) : "∞");

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

  private static class VariableTuple {
    private final long scopeKey;
    private final String name;

    VariableTuple(long scopeKey, String name) {
      this.scopeKey = scopeKey;
      this.name = name;
    }
  }
}
