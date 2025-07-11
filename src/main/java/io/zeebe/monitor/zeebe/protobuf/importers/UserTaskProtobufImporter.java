package io.zeebe.monitor.zeebe.protobuf.importers;

import io.camunda.zeebe.protocol.record.intent.Intent;
import io.camunda.zeebe.protocol.record.intent.UserTaskIntent;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.UserTaskEntity;
import io.zeebe.monitor.repository.UserTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserTaskProtobufImporter {

  @Autowired private UserTaskRepository userTaskRepository;

  public void importUserTask(final Schema.UserTaskRecord record) {

    final Intent intent = UserTaskIntent.valueOf(record.getMetadata().getIntent());
    final long timestamp = record.getMetadata().getTimestamp();

    UserTaskEntity userTask =
        userTaskRepository
            .findById(record.getUserTaskKey())
            .orElseGet(
                () -> {
                  final UserTaskEntity newEntity = new UserTaskEntity();
                  newEntity.setKey(record.getUserTaskKey());
                  newEntity.setBpmnProcessId(record.getBpmnProcessId());
                  newEntity.setElementId(record.getElementId());
                  newEntity.setProcessInstanceKey(record.getProcessInstanceKey());
                  newEntity.setElementInstanceKey(record.getElementInstanceKey());
                  return newEntity;
                });

    if (intent == UserTaskIntent.CREATED) {
      userTask.setStart(timestamp);
      userTask.setStatus("Active");
      userTaskRepository.save(userTask);
    } else if (intent == UserTaskIntent.COMPLETED) {
      userTask.setEnd(timestamp);
      userTask.setStatus("Completed");
      userTaskRepository.save(userTask);
    } else if (intent == UserTaskIntent.CANCELED) {
      userTask.setEnd(timestamp);
      userTask.setStatus("Cancelled");
      userTaskRepository.save(userTask);
    }
  }
}
