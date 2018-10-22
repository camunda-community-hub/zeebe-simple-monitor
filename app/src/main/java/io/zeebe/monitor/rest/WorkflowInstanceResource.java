/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.ActivityInstanceEntity;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.entity.WorkflowInstanceEntity;
import io.zeebe.monitor.repository.ActivityInstanceRepository;
import io.zeebe.monitor.repository.IncidentRepository;
import io.zeebe.monitor.repository.WorkflowInstanceRepository;
import io.zeebe.monitor.repository.WorkflowRepository;
import io.zeebe.monitor.zeebe.ZeebeConnectionService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instances")
public class WorkflowInstanceResource {

  @Autowired private ZeebeConnectionService connections;

  @Autowired private WorkflowInstanceRepository workflowInstanceRepository;

  @Autowired private ActivityInstanceRepository activityInstanceRepository;

  @Autowired private WorkflowRepository workflowRepository;

  @Autowired private IncidentRepository incidentRepository;

  @RequestMapping("/")
  public Iterable<WorkflowInstanceEntity> getWorkflowInstances() {
    return workflowInstanceRepository.findAll();
  }

  @RequestMapping("/{key}")
  public WorkflowInstanceDto getWorkflowInstance(@PathVariable("key") long key) {

    return workflowInstanceRepository
        .findByKey(key)
        .map(
            instance -> {
              final List<ActivityInstanceEntity> events =
                  StreamSupport.stream(
                          activityInstanceRepository.findByWorkflowInstanceKey(key).spliterator(),
                          false)
                      .collect(Collectors.toList());

              final ActivityInstanceEntity lastEvent = events.get(events.size() - 1);

              final WorkflowInstanceDto dto = new WorkflowInstanceDto();
              dto.setWorkflowInstanceKey(key);

              dto.setPartitionId(instance.getPartitionId());

              dto.setWorkflowKey(instance.getWorkflowKey());
              dto.setPayload(lastEvent.getPayload());

              dto.setBpmnProcessId(instance.getBpmnProcessId());
              dto.setWorkflowVersion(instance.getVersion());

              workflowRepository
                  .findByKey(instance.getWorkflowKey())
                  .ifPresent(
                      workflow -> {
                        dto.setWorkflowResource(workflow.getResource());
                      });

              final boolean isEnded = instance.getEnd() != null && instance.getEnd() > 0;
              dto.setEnded(isEnded);

              final List<String> completedActivities =
                  events
                      .stream()
                      .filter(
                          e ->
                              e.getIntent().equals("ELEMENT_COMPLETED")
                                  || e.getIntent().equals("ELEMENT_TERMINATED"))
                      .map(ActivityInstanceEntity::getActivityId)
                      .collect(Collectors.toList());
              dto.setEndedActivities(completedActivities);

              final List<String> activeAcitivities =
                  events
                      .stream()
                      .filter(e -> e.getIntent().equals("ELEMENT_ACTIVATED"))
                      .map(ActivityInstanceEntity::getActivityId)
                      .filter(id -> !completedActivities.contains(id))
                      .collect(Collectors.toList());
              dto.setRunningActivities(activeAcitivities);

              final List<String> takenSequenceFlows =
                  events
                      .stream()
                      .filter(e -> e.getIntent().equals("SEQUENCE_FLOW_TAKEN"))
                      .map(ActivityInstanceEntity::getActivityId)
                      .collect(Collectors.toList());
              dto.setTakenSequenceFlows(takenSequenceFlows);

              final List<IncidentEntity> incidents =
                  StreamSupport.stream(
                          incidentRepository.findByWorkflowInstanceKey(key).spliterator(), false)
                      .collect(Collectors.toList());

              incidents
                  .stream()
                  .collect(Collectors.groupingBy(IncidentEntity::getIncidentKey))
                  .entrySet()
                  .stream()
                  .forEach(
                      i -> {
                        final Long incidentKey = i.getKey();

                        final List<IncidentEntity> incidentEvents = i.getValue();
                        final IncidentEntity lastIncidentEvent =
                            incidentEvents.get(incidentEvents.size() - 1);

                        final IncidentDto incidentDto = new IncidentDto();
                        incidentDto.setKey(incidentKey);

                        events
                            .stream()
                            .filter(e -> e.getKey() == lastIncidentEvent.getActivityInstanceKey())
                            .findFirst()
                            .ifPresent(
                                e -> {
                                  incidentDto.setActivityId(e.getActivityId());
                                });

                        incidentDto.setActivityInstanceKey(
                            lastIncidentEvent.getActivityInstanceKey());
                        incidentDto.setJobKey(lastIncidentEvent.getJobKey());
                        incidentDto.setErrorType(lastIncidentEvent.getErrorType());
                        incidentDto.setErrorMessage(lastIncidentEvent.getErrorMessage());

                        final boolean isResolved =
                            lastIncidentEvent.getIntent().equals("RESOLVED")
                                || lastIncidentEvent.getIntent().equals("DELETED");
                        incidentDto.setResolved(isResolved);

                        if (!isResolved) {
                          dto.getIncidents().add(incidentDto);
                        }
                      });

              return dto;
            })
        .orElse(null);
  }

  @RequestMapping(path = "/{key}", method = RequestMethod.DELETE)
  public void cancelWorkflowInstance(@PathVariable("key") long key) throws Exception {
    connections.getClient().workflowClient().newCancelInstanceCommand(key).send().join();
  }

  @RequestMapping(path = "/{key}/update-payload", method = RequestMethod.PUT)
  public void updatePayload(@PathVariable("key") long key, @RequestBody String payload)
      throws Exception {
    connections
        .getClient()
        .workflowClient()
        .newUpdatePayloadCommand(key)
        .payload(payload)
        .send()
        .join();
  }

  @RequestMapping(path = "/{key}/update-retries", method = RequestMethod.PUT)
  public void updateRetries(@PathVariable("key") long key) throws Exception {

    final List<IncidentEntity> incidents =
        StreamSupport.stream(incidentRepository.findByWorkflowInstanceKey(key).spliterator(), false)
            .collect(Collectors.toList());

    incidents
        .stream()
        .collect(Collectors.groupingBy(IncidentEntity::getIncidentKey))
        .entrySet()
        .stream()
        .forEach(
            i -> {
              final List<IncidentEntity> incidentEvents = i.getValue();
              final IncidentEntity lastIncidentEvent =
                  incidentEvents.get(incidentEvents.size() - 1);

              final long jobKey = lastIncidentEvent.getJobKey();

              final boolean isResolved =
                  lastIncidentEvent.getIntent().equals("RESOLVED")
                      || lastIncidentEvent.getIntent().equals("DELETED");

              if (!isResolved && jobKey > 0) {
                connections
                    .getClient()
                    .jobClient()
                    .newUpdateRetriesCommand(jobKey)
                    .retries(2)
                    .send()
                    .join();
              }
            });
  }
}
