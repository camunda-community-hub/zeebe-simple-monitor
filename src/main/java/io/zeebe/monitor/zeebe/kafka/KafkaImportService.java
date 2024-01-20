package io.zeebe.monitor.zeebe.kafka;

import static io.camunda.zeebe.protocol.record.ValueType.*;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.ValueType;
import io.zeebe.monitor.zeebe.kafka.importers.ErrorKafkaImporter;
import io.zeebe.monitor.zeebe.kafka.importers.IncidentKafkaImporter;
import io.zeebe.monitor.zeebe.kafka.importers.JobKafkaImporter;
import io.zeebe.monitor.zeebe.kafka.importers.KafkaImporter;
import io.zeebe.monitor.zeebe.kafka.importers.MessageKafkaImporter;
import io.zeebe.monitor.zeebe.kafka.importers.MessageStartEventSubscriptionKafkaImporter;
import io.zeebe.monitor.zeebe.kafka.importers.MessageSubscriptionKafkaImporter;
import io.zeebe.monitor.zeebe.kafka.importers.ProcessInstanceKafkaImporter;
import io.zeebe.monitor.zeebe.kafka.importers.ProcessKafkaImporter;
import io.zeebe.monitor.zeebe.kafka.importers.TimerKafkaImporter;
import io.zeebe.monitor.zeebe.kafka.importers.VariableKafkaImporter;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KafkaImportService {

  private static final Logger LOG = LoggerFactory.getLogger(KafkaImportService.class);

  @Autowired private ErrorKafkaImporter errorImporter;
  @Autowired private IncidentKafkaImporter incidentImporter;
  @Autowired private JobKafkaImporter jobImporter;
  @Autowired private MessageKafkaImporter messageImporter;
  @Autowired private MessageSubscriptionKafkaImporter messageSubscriptionImporter;
  @Autowired private MessageStartEventSubscriptionKafkaImporter messageStartEventSubscriptionImporter;
  @Autowired private ProcessKafkaImporter processImporter;
  @Autowired private ProcessInstanceKafkaImporter processInstanceImporter;
  @Autowired private TimerKafkaImporter timerImporter;
  @Autowired private VariableKafkaImporter variableImporter;

  private final EnumSet<ValueType> availableValueTypes =
      EnumSet.of(
          ERROR,
          JOB,
          INCIDENT,
          MESSAGE,
          MESSAGE_SUBSCRIPTION,
          MESSAGE_START_EVENT_SUBSCRIPTION,
          PROCESS,
          PROCESS_INSTANCE,
          TIMER,
          VARIABLE);

  /**
   * Saves in separate transactions on each value type {@link #runAsync(KafkaImporter, ValueType, List)}
   * to do it asynchronously.
   * If some transactions rollbacks by an exception, previous executed will
   * not.
   * After the exception, invoker can save the entire batch again. It's ok, all operations is
   * idempotent
   *
   * @param records records to save
   */
  public void save(List<Record<RecordValue>> records) {
    var tasks =
        records.stream().collect(Collectors.groupingBy(Record::getValueType)).entrySet().stream()
            .map(
                entry -> {
                  var valueType = entry.getKey();
                  var recordList = entry.getValue();
                  return switch (valueType) {
                    case ERROR -> runAsync(errorImporter, valueType, recordList);
                    case INCIDENT -> runAsync(incidentImporter, valueType, recordList);
                    case JOB -> runAsync(jobImporter, valueType, recordList);
                    case MESSAGE -> runAsync(messageImporter, valueType, recordList);
                    case MESSAGE_SUBSCRIPTION -> runAsync(
                        messageSubscriptionImporter, valueType, recordList);
                    case MESSAGE_START_EVENT_SUBSCRIPTION -> runAsync(
                        messageStartEventSubscriptionImporter, valueType, recordList);
                    case PROCESS -> runAsync(processImporter, valueType, recordList);
                    case PROCESS_INSTANCE -> runAsync(
                        processInstanceImporter, valueType, recordList);
                    case TIMER -> runAsync(timerImporter, valueType, recordList);
                    case VARIABLE -> runAsync(variableImporter, valueType, recordList);
                    default -> CompletableFuture.completedFuture(Void.TYPE);
                  };
                })
            .toList();
    CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
  }

  public boolean isAvailableType(ValueType valueType) {
    return availableValueTypes.contains(valueType);
  }

  private CompletableFuture<Void> runAsync(
          KafkaImporter kafkaImporter, ValueType valueType, List<Record<RecordValue>> recordList) {
    return CompletableFuture.runAsync(
        () -> {
          kafkaImporter.importRecords(recordList);
          logImported(valueType, recordList);
        });
  }

  private void logImported(ValueType valueType, List<Record<RecordValue>> recordList) {
    LOG.debug("Imported: {} records with type '{}'", recordList.size(), valueType);
  }
}
