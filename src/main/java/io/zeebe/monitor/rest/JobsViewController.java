package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.entity.JobEntity;
import io.zeebe.monitor.repository.IncidentRepository;
import io.zeebe.monitor.repository.JobRepository;
import io.zeebe.monitor.rest.dto.IncidentListDto;
import io.zeebe.monitor.rest.dto.JobDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.*;

@Controller
public class JobsViewController extends AbstractViewController {

  private static final List<String> JOB_COMPLETED_INTENTS = Arrays.asList("completed", "canceled");

  @Autowired private JobRepository jobRepository;

  @GetMapping("/views/jobs")
  public String jobList(final Map<String, Object> model, final Pageable pageable) {

    final long count = jobRepository.countByStateNotIn(JOB_COMPLETED_INTENTS);

    final List<JobDto> dtos = new ArrayList<>();
    for (final JobEntity jobEntity :
        jobRepository.findByStateNotIn(JOB_COMPLETED_INTENTS, pageable)) {
      final JobDto dto = toDto(jobEntity);
      dtos.add(dto);
    }

    model.put("jobs", dtos);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);
    addDefaultAttributesToModel(model);

    return "job-list-view";
  }

  static JobDto toDto(final JobEntity job) {
    final JobDto dto = new JobDto();

    dto.setKey(job.getKey());
    dto.setJobType(job.getJobType());
    dto.setProcessInstanceKey(job.getProcessInstanceKey());
    dto.setElementInstanceKey(job.getElementInstanceKey());
    dto.setState(job.getState());
    dto.setRetries(job.getRetries());
    Optional.ofNullable(job.getWorker()).ifPresent(dto::setWorker);
    dto.setTimestamp(Instant.ofEpochMilli(job.getTimestamp()).toString());

    return dto;
  }

}
