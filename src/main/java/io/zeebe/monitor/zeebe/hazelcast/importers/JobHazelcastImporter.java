package io.zeebe.monitor.zeebe.hazelcast.importers;

import io.camunda.zeebe.protocol.record.intent.JobIntent;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.JobEntity;
import io.zeebe.monitor.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobHazelcastImporter {

  @Autowired private JobRepository jobRepository;

  public void importJob(final Schema.JobRecord record) {

    final JobIntent intent = JobIntent.valueOf(record.getMetadata().getIntent());
    final long key = record.getMetadata().getKey();
    final long timestamp = record.getMetadata().getTimestamp();

    final JobEntity entity =
        jobRepository
            .findById(key)
            .orElseGet(
                () -> {
                  final JobEntity newEntity = new JobEntity();
                  newEntity.setKey(key);
                  newEntity.setProcessInstanceKey(record.getProcessInstanceKey());
                  newEntity.setElementInstanceKey(record.getElementInstanceKey());
                  newEntity.setJobType(record.getType());
                  return newEntity;
                });

    entity.setState(intent.name().toLowerCase());
    entity.setTimestamp(timestamp);
    entity.setWorker(record.getWorker());
    entity.setRetries(record.getRetries());
    jobRepository.save(entity);
  }
}
