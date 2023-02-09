package io.zeebe.monitor.zeebe.kafka.importers;

import io.camunda.zeebe.protocol.record.intent.JobIntent;
import io.zeebe.monitor.entity.JobEntity;
import io.zeebe.monitor.repository.JobRepository;
import io.zeebe.monitor.zeebe.kafka.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaJobImporter {

  @Autowired private JobRepository jobRepository;

  public void importJob(final GenericRecord record) {

    final JobIntent intent = JobIntent.valueOf(record.getIntent());
    final long key = record.getKey();
    final long timestamp = record.getTimestamp();
    Map values = record.getValue();
    final JobEntity entity =
        jobRepository
            .findById(key)
            .orElseGet(
                () -> {
                  final JobEntity newEntity = new JobEntity();
                  newEntity.setKey(key);
                  newEntity.setProcessInstanceKey(values.get("processInstanceKey") != null ? ((Number)values.get("processInstanceKey")).longValue() : 0);
                  newEntity.setElementInstanceKey(values.get("elementInstanceKey") != null ? ((Number)values.get("elementInstanceKey")).longValue() : 0);
                  newEntity.setJobType((String)values.get("type"));
                  return newEntity;
                });

    entity.setState(intent.name().toLowerCase());
    entity.setTimestamp(timestamp);
    entity.setWorker((String)values.get("worker"));
    entity.setRetries(values.get("retries") != null ? ((Number)values.get("retries")).intValue() : 0);
    jobRepository.save(entity);
  }

}
