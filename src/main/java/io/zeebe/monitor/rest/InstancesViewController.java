package io.zeebe.monitor.rest;

import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.instance.FlowElement;
import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import io.zeebe.monitor.entity.ElementInstanceEntity;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.entity.VariableEntity;
import io.zeebe.monitor.repository.*;
import io.zeebe.monitor.rest.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static io.zeebe.monitor.rest.ProcessesViewController.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class InstancesViewController extends AbstractViewController {

  private static final String WARNING_NO_XML_RESOURCE_FOUND = "WARNING-NO-XML-RESOURCE-FOUND";

  @Autowired private ProcessRepository processRepository;
  @Autowired private ProcessInstanceRepository processInstanceRepository;
  @Autowired private ElementInstanceRepository activityInstanceRepository;
  @Autowired private IncidentRepository incidentRepository;
  @Autowired private JobRepository jobRepository;
  @Autowired private MessageSubscriptionRepository messageSubscriptionRepository;
  @Autowired private TimerRepository timerRepository;
  @Autowired private VariableRepository variableRepository;
  @Autowired private ErrorRepository errorRepository;

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

  @GetMapping("/views/instances/{key}")
  @Transactional
  public String instanceDetail(
      @PathVariable final long key, final Map<String, Object> model, final Pageable pageable) {

    final ProcessInstanceEntity instance =
        processInstanceRepository
            .findByKey(key)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No process instance found with key: " + key));

    model.put("resource", WARNING_NO_XML_RESOURCE_FOUND);
    processRepository
        .findByKey(instance.getProcessDefinitionKey())
        .ifPresent(process -> model.put("resource", ProcessesViewController.getProcessResource(process)));

    model.put("instance", toInstanceDto(instance));
    addDefaultAttributesToModel(model);

    return "instance-detail-view";
  }

  private ProcessInstanceDto toInstanceDto(final ProcessInstanceEntity instance) {
    final List<ElementInstanceEntity> events =
        StreamSupport.stream(
                activityInstanceRepository
                    .findByProcessInstanceKey(instance.getKey())
                    .spliterator(),
                false)
            .collect(Collectors.toList());

    final ProcessInstanceDto dto = new ProcessInstanceDto();
    dto.setProcessInstanceKey(instance.getKey());

    dto.setPartitionId(instance.getPartitionId());

    dto.setProcessDefinitionKey(instance.getProcessDefinitionKey());

    dto.setBpmnProcessId(instance.getBpmnProcessId());
    dto.setVersion(instance.getVersion());

    final boolean isEnded = instance.getEnd() != null && instance.getEnd() > 0;
    dto.setState(instance.getState());
    dto.setRunning(!isEnded);

    dto.setStartTime(Instant.ofEpochMilli(instance.getStart()).toString());

    if (isEnded) {
      dto.setEndTime(Instant.ofEpochMilli(instance.getEnd()).toString());
    }

    if (instance.getParentElementInstanceKey() > 0) {
      dto.setParentProcessInstanceKey(instance.getParentProcessInstanceKey());

      processInstanceRepository
          .findByKey(instance.getParentProcessInstanceKey())
          .ifPresent(
              parent -> {
                dto.setParentBpmnProcessId(parent.getBpmnProcessId());
              });
    }

    final List<String> completedActivities =
        events.stream()
            .filter(e -> PROCESS_INSTANCE_COMPLETED_INTENTS.contains(e.getIntent()))
            .filter(e -> !e.getBpmnElementType().equals(BpmnElementType.PROCESS.name()))
            .map(ElementInstanceEntity::getElementId)
            .collect(Collectors.toList());

    final List<String> activeActivities =
        events.stream()
            .filter(e -> PROCESS_INSTANCE_ENTERED_INTENTS.contains(e.getIntent()))
            .filter(e -> !e.getBpmnElementType().equals(BpmnElementType.PROCESS.name()))
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
            .filter(e -> PROCESS_INSTANCE_COMPLETED_INTENTS.contains(e.getIntent()))
            .filter(e -> !EXCLUDE_ELEMENT_TYPES.contains(e.getBpmnElementType()))
            .collect(
                Collectors.groupingBy(ElementInstanceEntity::getElementId, Collectors.counting()));

    final Map<String, Long> enteredElementsById =
        events.stream()
            .filter(e -> PROCESS_INSTANCE_ENTERED_INTENTS.contains(e.getIntent()))
            .filter(e -> !EXCLUDE_ELEMENT_TYPES.contains(e.getBpmnElementType()))
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

          dto.setBpmnElementInfos(ProcessesViewController.getBpmnElementInfos(bpmn));
        });

    final List<AuditLogEntry> auditLogEntries =
        events.stream()
            .map(
                e -> {
                  final AuditLogEntry entry = new AuditLogEntry();

                  entry.setKey(e.getKey());
                  entry.setFlowScopeKey(e.getFlowScopeKey());
                  entry.setElementId(e.getElementId());
                  entry.setElementName(flowElements.getOrDefault(e.getElementId(), ""));
                  entry.setBpmnElementType(e.getBpmnElementType());
                  entry.setState(e.getIntent());
                  entry.setTimestamp(Instant.ofEpochMilli(e.getTimestamp()).toString());

                  return entry;
                })
            .collect(Collectors.toList());

    dto.setAuditLogEntries(auditLogEntries);

    final List<IncidentEntity> incidents =
        StreamSupport.stream(
                incidentRepository.findByProcessInstanceKey(instance.getKey()).spliterator(), false)
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
        variableRepository.findByProcessInstanceKey(instance.getKey()).stream()
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

    final List<ActiveScope> activeScopes = new ArrayList<>();
    if (!isEnded) {
      activeScopes.add(new ActiveScope(instance.getKey(), instance.getBpmnProcessId()));

      final List<Long> completedElementInstances =
          events.stream()
              .filter(e -> PROCESS_INSTANCE_COMPLETED_INTENTS.contains(e.getIntent()))
              .map(ElementInstanceEntity::getKey)
              .collect(Collectors.toList());

      final List<ActiveScope> activeElementInstances =
          events.stream()
              .filter(e -> PROCESS_INSTANCE_ENTERED_INTENTS.contains(e.getIntent()))
              .map(ElementInstanceEntity::getKey)
              .filter(id -> !completedElementInstances.contains(id))
              .map(scopeKey -> new ActiveScope(scopeKey, elementIdsForKeys.get(scopeKey)))
              .collect(Collectors.toList());

      activeScopes.addAll(activeElementInstances);
    }
    dto.setActiveScopes(activeScopes);

    final List<JobDto> jobDtos =
        jobRepository.findByProcessInstanceKey(instance.getKey()).stream()
            .map(
                job -> {
                  final JobDto jobDto = JobsViewController.toDto(job);
                  jobDto.setElementId(
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
        messageSubscriptionRepository.findByProcessInstanceKey(instance.getKey()).stream()
            .map(
                subscription -> {
                  final MessageSubscriptionDto subscriptionDto = ProcessesViewController.toDto(subscription);
                  subscriptionDto.setElementId(
                      elementIdsForKeys.getOrDefault(subscriptionDto.getElementInstanceKey(), ""));

                  return subscriptionDto;
                })
            .collect(Collectors.toList());
    dto.setMessageSubscriptions(messageSubscriptions);

    final List<TimerDto> timers =
        timerRepository.findByProcessInstanceKey(instance.getKey()).stream()
            .map(ProcessesViewController::toDto)
            .collect(Collectors.toList());
    dto.setTimers(timers);

    final var calledProcessInstances =
        processInstanceRepository.findByParentProcessInstanceKey(instance.getKey()).stream()
            .map(
                childEntity -> {
                  final var childDto = new CalledProcessInstanceDto();

                  childDto.setChildProcessInstanceKey(childEntity.getKey());
                  childDto.setChildBpmnProcessId(childEntity.getBpmnProcessId());
                  childDto.setChildState(childEntity.getState());

                  childDto.setElementInstanceKey(childEntity.getParentElementInstanceKey());

                  final var callElementId =
                      elementIdsForKeys.getOrDefault(childEntity.getParentElementInstanceKey(), "");
                  childDto.setElementId(callElementId);

                  return childDto;
                })
            .collect(Collectors.toList());
    dto.setCalledProcessInstances(calledProcessInstances);

    final var errors =
        errorRepository.findByProcessInstanceKey(instance.getKey()).stream()
            .map(ErrorsViewController::toDto)
            .collect(Collectors.toList());
    dto.setErrors(errors);

    return dto;
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
