package io.zeebe.monitor.zeebe.kafka.importers;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public abstract class KafkaImporter {

  abstract void importRecord(final Record<RecordValue> record);

  @Transactional
  public void importRecords(List<Record<RecordValue>> records) {
    records.forEach(this::importRecord);
  }

  protected String generateId() {
    return UUID.randomUUID().toString();
  }
}
