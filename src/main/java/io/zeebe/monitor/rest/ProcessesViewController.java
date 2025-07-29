package io.zeebe.monitor.rest;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.instance.CatchEvent;
import io.camunda.zeebe.model.bpmn.instance.ErrorEventDefinition;
import io.camunda.zeebe.model.bpmn.instance.SequenceFlow;
import io.camunda.zeebe.model.bpmn.instance.ServiceTask;
import io.camunda.zeebe.model.bpmn.instance.TimerEventDefinition;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskDefinition;
import io.camunda.zeebe.protocol.record.intent.MessageSubscriptionIntent;
import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import io.zeebe.monitor.entity.ElementInstanceStatistics;
import io.zeebe.monitor.entity.MessageSubscriptionEntity;
import io.zeebe.monitor.entity.ProcessEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.entity.TimerEntity;
import io.zeebe.monitor.repository.MessageSubscriptionRepository;
import io.zeebe.monitor.repository.ProcessInstanceRepository;
import io.zeebe.monitor.repository.ProcessRepository;
import io.zeebe.monitor.repository.TimerRepository;
import io.zeebe.monitor.rest.dto.BpmnElementInfo;
import io.zeebe.monitor.rest.dto.ElementInstanceState;
import io.zeebe.monitor.rest.dto.MessageSubscriptionDto;
import io.zeebe.monitor.rest.dto.ProcessDto;
import io.zeebe.monitor.rest.dto.ProcessInstanceListDto;
import io.zeebe.monitor.rest.dto.TimerDto;
import jakarta.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ProcessesViewController extends AbstractViewController {

  static final List<String> PROCESS_INSTANCE_ENTERED_INTENTS = List.of("ELEMENT_ACTIVATED");
  static final List<String> PROCESS_INSTANCE_COMPLETED_INTENTS = List.of("ELEMENT_COMPLETED", "ELEMENT_TERMINATED");
  static final List<String> EXCLUDE_ELEMENT_TYPES = List.of(BpmnElementType.MULTI_INSTANCE_BODY.name());

  @Autowired
  private ProcessRepository processRepository;
  @Autowired
  private ProcessInstanceRepository processInstanceRepository;
  @Autowired
  private MessageSubscriptionRepository messageSubscriptionRepository;
  @Autowired
  private TimerRepository timerRepository;

  @GetMapping("/")
  public String index(final Map<String, Object> model, final Pageable pageable) {
    return processList(model, pageable);
  }

  @GetMapping("/views/processes")
  public String processList(final Map<String, Object> model, final Pageable pageable) {

    final long count = processRepository.count();

    final List<ProcessDto> processes = new ArrayList<>();
    for (final ProcessEntity processEntity : processRepository.findAll(pageable)) {
      final ProcessDto dto = ProcessDto.from(processEntity, 0, 0);
      processes.add(dto);
    }

    model.put("processes", processes);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);
    addDefaultAttributesToModel(model);

    return "process-list-view";
  }

  @GetMapping("/views/processes/{key}")
  @Transactional
  public String processDetail(
      @PathVariable("key") final long key, final Map<String, Object> model, final Pageable pageable) {

    final ProcessEntity process = processRepository
        .findByKey(key)
        .orElseThrow(
            () -> new ResponseStatusException(NOT_FOUND, "No process found with key: " + key));

    model.put("process", toDto(process));
    model.put("resource", getProcessResource(process));

    final List<ElementInstanceState> elementInstanceStates = getElementInstanceStates(key);
    model.put("instance.elementInstances", elementInstanceStates);

    final long count =10000L;// processInstanceRepository.countByProcessDefinitionKey(key);

    final List<ProcessInstanceListDto> instances = new ArrayList<>();
    for (final ProcessInstanceEntity instanceEntity : processInstanceRepository.findByProcessDefinitionKey(key,
        pageable)) {
      instances.add(toDto(instanceEntity));
    }

    model.put("instances", instances);
    model.put("count", count);

    final List<TimerDto> timers = timerRepository.findByProcessDefinitionKeyAndProcessInstanceKeyIsNull(key).stream()
        .map(ProcessesViewController::toDto)
        .collect(Collectors.toList());
    model.put("timers", timers);

    final List<MessageSubscriptionDto> messageSubscriptions = messageSubscriptionRepository
        .findByProcessDefinitionKeyAndProcessInstanceKeyIsNull(key)
        .stream()
        .map(ProcessesViewController::toDto)
        .collect(Collectors.toList());
    model.put("messageSubscriptions", messageSubscriptions);

    if (process.getResourcetext() != null) {

      final var resourceAsStream = new ByteArrayInputStream(process.getResourcetext().getBytes());
      final var bpmn = Bpmn.readModelFromStream(resourceAsStream);
      model.put("instance.bpmnElementInfos", getBpmnElementInfos(bpmn));

    } else {

      final var resourceAsStream = new ByteArrayInputStream(process.getResource().getBytes());
      final var bpmn = Bpmn.readModelFromStream(resourceAsStream);
      model.put("instance.bpmnElementInfos", getBpmnElementInfos(bpmn));
      process.setResourcetext(process.getResource());
      processRepository.save(process);

    }

    addPaginationToModel(model, pageable, count);
    addDefaultAttributesToModel(model);

    return "process-detail-view";
  }

  ProcessDto toDto(final ProcessEntity processEntity) {
    final long processDefinitionKey = processEntity.getKey();

    final long running = processInstanceRepository.countByProcessDefinitionKeyAndEndIsNull(processDefinitionKey);
    final long ended = processInstanceRepository.countByProcessDefinitionKeyAndEndIsNotNull(processDefinitionKey);

    return ProcessDto.from(processEntity, running, ended);
  }

  static ProcessInstanceListDto toDto(final ProcessInstanceEntity instance) {

    final ProcessInstanceListDto dto = new ProcessInstanceListDto();
    dto.setProcessInstanceKey(instance.getKey());

    dto.setBpmnProcessId(instance.getBpmnProcessId());
    dto.setProcessDefinitionKey(instance.getProcessDefinitionKey());

    final boolean isEnded = instance.getEnd() != null && instance.getEnd() > 0;
    dto.setState(instance.getState());

    dto.setStartTime(Instant.ofEpochMilli(instance.getStart()).toString());

    if (isEnded) {
      dto.setEndTime(Instant.ofEpochMilli(instance.getEnd()).toString());
    }

    return dto;
  }

  static TimerDto toDto(final TimerEntity timer) {
    final TimerDto dto = new TimerDto();

    dto.setElementId(timer.getTargetElementId());
    dto.setState(timer.getState());
    dto.setDueDate(Instant.ofEpochMilli(timer.getDueDate()).toString());
    dto.setTimestamp(Instant.ofEpochMilli(timer.getTimestamp()).toString());
    dto.setElementInstanceKey(timer.getElementInstanceKey());

    final int repetitions = timer.getRepetitions();
    dto.setRepetitions(repetitions >= 0 ? String.valueOf(repetitions) : "∞");

    return dto;
  }

  static MessageSubscriptionDto toDto(final MessageSubscriptionEntity subscription) {
    final MessageSubscriptionDto dto = new MessageSubscriptionDto();

    dto.setKey(subscription.getId());
    dto.setMessageName(subscription.getMessageName());
    dto.setCorrelationKey(Optional.ofNullable(subscription.getCorrelationKey()).orElse(""));

    dto.setProcessInstanceKey(subscription.getProcessInstanceKey());
    dto.setElementInstanceKey(subscription.getElementInstanceKey());

    dto.setElementId(subscription.getTargetFlowNodeId());

    dto.setState(subscription.getState());
    dto.setTimestamp(Instant.ofEpochMilli(subscription.getTimestamp()).toString());

    dto.setOpen(subscription.getState().equalsIgnoreCase(MessageSubscriptionIntent.CREATED.name()));

    return dto;
  }

  static String getProcessResource(final ProcessEntity process) {
    final var resource = process.getResource();
    // replace all backticks because they are used to enclose the content of the
    // BPMN in the HTML
    return resource.replaceAll("`", "\"");
  }

  private List<ElementInstanceState> getElementInstanceStates(final long key) {

    final List<ElementInstanceStatistics> elementEnteredStatistics = processRepository
        .getElementInstanceStatisticsByKeyAndIntentIn(
            key, PROCESS_INSTANCE_ENTERED_INTENTS, EXCLUDE_ELEMENT_TYPES);

    final Map<String, Long> elementCompletedCount = processRepository
        .getElementInstanceStatisticsByKeyAndIntentIn(
            key, PROCESS_INSTANCE_COMPLETED_INTENTS, EXCLUDE_ELEMENT_TYPES)
        .stream()
        .collect(
            Collectors.toMap(
                ElementInstanceStatistics::getElementId, ElementInstanceStatistics::getCount));

    return elementEnteredStatistics.stream()
        .map(
            s -> {
              final ElementInstanceState state = new ElementInstanceState();

              final String elementId = s.getElementId();
              state.setElementId(elementId);

              final long completedInstances = elementCompletedCount.getOrDefault(elementId, 0L);
              final long enteredInstances = s.getCount();

              state.setActiveInstances(enteredInstances - completedInstances);
              state.setEndedInstances(completedInstances);

              return state;
            })
        .collect(Collectors.toList());
  }

  static List<BpmnElementInfo> getBpmnElementInfos(final BpmnModelInstance bpmn) {
    final List<BpmnElementInfo> infos = new ArrayList<>();

    bpmn.getModelElementsByType(ServiceTask.class)
        .forEach(
            t -> {
              final var info = new BpmnElementInfo();
              info.setElementId(t.getId());
              final var jobType = t.getSingleExtensionElement(ZeebeTaskDefinition.class).getType();
              info.setInfo("job-type: " + jobType);

              infos.add(info);
            });

    bpmn.getModelElementsByType(SequenceFlow.class)
        .forEach(
            s -> {
              final var conditionExpression = s.getConditionExpression();

              if (conditionExpression != null && !conditionExpression.getTextContent().isEmpty()) {

                final var info = new BpmnElementInfo();
                info.setElementId(s.getId());
                final var condition = conditionExpression.getTextContent();
                info.setInfo("condition: " + condition);

                infos.add(info);
              }
            });

    bpmn.getModelElementsByType(CatchEvent.class)
        .forEach(
            catchEvent -> {
              final var info = new BpmnElementInfo();
              info.setElementId(catchEvent.getId());

              catchEvent
                  .getEventDefinitions()
                  .forEach(
                      eventDefinition -> {
                        if (eventDefinition instanceof ErrorEventDefinition errorEventDefinition) {
                          if (errorEventDefinition.getError() != null) {
                            info.setInfo("errorCode: " + errorEventDefinition.getError().getErrorCode());
                          } else {
                            info.setInfo("errorCode: <null>");
                          }
                          infos.add(info);
                        }

                        if (eventDefinition instanceof TimerEventDefinition timerEventDefinition) {

                          Optional.<ModelElementInstance>ofNullable(
                              timerEventDefinition.getTimeCycle())
                              .or(() -> Optional.ofNullable(timerEventDefinition.getTimeDate()))
                              .or(() -> Optional.ofNullable(timerEventDefinition.getTimeDuration()))
                              .map(ModelElementInstance::getTextContent)
                              .ifPresent(
                                  timer -> {
                                    info.setInfo("timer: " + timer);
                                    infos.add(info);
                                  });
                        }
                      });
            });

    return infos;
  }
}
