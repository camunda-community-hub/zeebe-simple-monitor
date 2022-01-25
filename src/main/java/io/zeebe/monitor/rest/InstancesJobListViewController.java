package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.ElementInstanceEntity;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.repository.JobRepository;
import io.zeebe.monitor.rest.dto.JobDto;
import io.zeebe.monitor.rest.dto.ProcessInstanceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class InstancesJobListViewController extends AbstractInstanceViewController {

  @Autowired
  private JobRepository jobRepository;

  @GetMapping("/views/instances/{key}/job-list")
  @Transactional
  public String instanceDetailJobList(
      @PathVariable final long key, final Map<String, Object> model, @PageableDefault(size = DETAIL_LIST_SIZE) final Pageable pageable) {

    initializeProcessInstanceDto(key, model, pageable);
    model.put("content-job-list-view", new EnableConditionalViewRenderer());
    return "instance-detail-view";
  }

  @Override
  protected void fillViewDetailsIntoDto(ProcessInstanceEntity instance, List<ElementInstanceEntity> events, List<IncidentEntity> incidents, Map<Long, String> elementIdsForKeys, Map<String, Object> model, Pageable pageable, ProcessInstanceDto dto) {
    final List<JobDto> jobDtos =
        jobRepository.findByProcessInstanceKey(instance.getKey(), pageable).stream()
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

    final long count = jobRepository.countByProcessInstanceKey(instance.getKey());
    this.addPaginationToModel(model, pageable, count);
  }
}
