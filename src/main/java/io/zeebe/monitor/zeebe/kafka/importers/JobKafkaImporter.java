package io.zeebe.monitor.zeebe.kafka.importers;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.value.JobRecordValue;
import io.zeebe.monitor.entity.JobEntity;
import io.zeebe.monitor.repository.JobRepository;
import io.zeebe.monitor.zeebe.event.JobEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class JobKafkaImporter extends KafkaImporter {

  @Autowired private JobRepository jobRepository;
  @Autowired private ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void importRecord(final Record<RecordValue> record) {
    final var value = (JobRecordValue) record.getValue();
    final var intent = record.getIntent();
    final long key = record.getKey();
    final long timestamp = record.getTimestamp();

    final var entity =
        jobRepository
            .findById(key)
            .orElseGet(
                () -> {
                  final var newEntity = new JobEntity();
                  newEntity.setKey(key);
                  newEntity.setProcessInstanceKey(value.getProcessInstanceKey());
                  newEntity.setElementInstanceKey(value.getElementInstanceKey());
                  newEntity.setJobType(value.getType());
                  return newEntity;
                });

    entity.setState(intent.name().toLowerCase());
    entity.setTimestamp(timestamp);
    entity.setWorker(value.getWorker());
    entity.setRetries(value.getRetries());
    jobRepository.save(entity);

    applicationEventPublisher.publishEvent(new JobEvent());
  }
}
