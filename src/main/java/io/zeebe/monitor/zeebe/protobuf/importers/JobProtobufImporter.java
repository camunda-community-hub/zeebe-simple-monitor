package io.zeebe.monitor.zeebe.protobuf.importers;

import io.camunda.zeebe.protocol.record.intent.JobIntent;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.JobEntity;
import io.zeebe.monitor.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobProtobufImporter {

  @Autowired
  private JobRepository jobRepository;

  public void importJob(final Schema.JobRecord record) {

    final JobIntent intent = JobIntent.valueOf(record.getMetadata().getIntent());
    if (intent != JobIntent.COMPLETED) {
      return;
    }
    final long key = record.getMetadata().getKey();
    final long timestamp = record.getMetadata().getTimestamp();

    final JobEntity newEntity = new JobEntity();
    newEntity.setKey(key);
    newEntity.setProcessInstanceKey(record.getProcessInstanceKey());
    newEntity.setElementInstanceKey(record.getElementInstanceKey());
    newEntity.setJobType(record.getType());

    newEntity.setState(intent.name().toLowerCase());
    newEntity.setTimestamp(timestamp);
    newEntity.setWorker(record.getWorker());
    newEntity.setRetries(record.getRetries());
    jobRepository.save(newEntity);
  }
}
