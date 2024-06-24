package io.zeebe.monitor.zeebe.importers;

import io.camunda.zeebe.protocol.record.intent.JobIntent;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.JobEntity;
import io.zeebe.monitor.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class JobImporter {

    @Autowired
    private JobRepository jobRepository;
    private static final Logger LOG = LoggerFactory.getLogger(JobImporter.class);

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
        try {
            jobRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            LOG.warn("Attempted to save duplicate Job with key {}", entity.getKey());
        }
    }

}
