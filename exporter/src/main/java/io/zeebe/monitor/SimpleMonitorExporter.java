/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.monitor;

import io.zeebe.exporter.api.Exporter;
import io.zeebe.exporter.api.context.Context;
import io.zeebe.exporter.api.context.Controller;
import io.zeebe.protocol.Protocol;
import io.zeebe.protocol.record.Record;
import io.zeebe.protocol.record.RecordType;
import io.zeebe.protocol.record.ValueType;
import io.zeebe.protocol.record.intent.*;
import io.zeebe.protocol.record.value.*;
import io.zeebe.protocol.record.value.deployment.DeployedWorkflow;
import io.zeebe.protocol.record.value.deployment.DeploymentResource;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SimpleMonitorExporter implements Exporter {

  private static final String ENV_PREFIX = "SIMPLE_MONITOR_EXPORTER_";
  private static final String ENV_JDBC_URL = ENV_PREFIX + "JDBC_URL";
  private static final String ENV_JDBC_DRIVER = ENV_PREFIX + "JDBC_DRIVER";
  private static final String ENV_JDBC_USER = ENV_PREFIX + "JDBC_USER";
  private static final String ENV_JDBC_PASSWORD = ENV_PREFIX + "JDBC_PASSWORD";

  private static final String INSERT_WORKFLOW =
      "INSERT INTO WORKFLOW (ID_, KEY_, BPMN_PROCESS_ID_, VERSION_, RESOURCE_, TIMESTAMP_) VALUES ('%s', %d, '%s', %d, '%s', %d);";

  private static final String INSERT_WORKFLOW_INSTANCE =
      "INSERT INTO WORKFLOW_INSTANCE"
          + " (ID_, PARTITION_ID_, KEY_, BPMN_PROCESS_ID_, VERSION_, WORKFLOW_KEY_, STATE_, START_)"
          + " VALUES "
          + "('%s', %d, %d, '%s', %d, %d, '%s', %d);";

  private static final String UPDATE_WORKFLOW_INSTANCE =
      "UPDATE WORKFLOW_INSTANCE SET END_ = %d, STATE_ = '%s' WHERE KEY_ = %d;";

  private static final String INSERT_ELEMENT_INSTANCE =
      "INSERT INTO ELEMENT_INSTANCE"
          + " (ID_, PARTITION_ID_, KEY_, INTENT_, WORKFLOW_INSTANCE_KEY_, ELEMENT_ID_, FLOW_SCOPE_KEY_, WORKFLOW_KEY_, TIMESTAMP_)"
          + " VALUES "
          + "('%s', %d, %d, '%s', %d, '%s', %d, %d, %d);";

  private static final String INSERT_INCIDENT =
      "INSERT INTO INCIDENT"
          + " (ID_, KEY_, BPMN_PROCESS_ID_, WORKFLOW_KEY_, WORKFLOW_INSTANCE_KEY_, ELEMENT_INSTANCE_KEY_, JOB_KEY_, ERROR_TYPE_, ERROR_MSG_, CREATED_)"
          + " VALUES "
          + "('%s', %d, '%s', %d, %d, %d, %d, '%s', '%s', %d)";

  private static final String UPDATE_INCIDENT =
      "UPDATE INCIDENT SET RESOLVED_ = %d WHERE KEY_ = %d;";

  private static final String INSERT_JOB =
      "INSERT INTO JOB"
          + " (ID_, KEY_, JOB_TYPE_, WORKFLOW_INSTANCE_KEY_, ELEMENT_INSTANCE_KEY_, STATE_, RETRIES_, TIMESTAMP_)"
          + " VALUES "
          + "('%s', %d, '%s', %d, %d, '%s', %d, %d)";

  private static final String UPDATE_JOB =
      "UPDATE JOB SET STATE_ = '%s', WORKER_ = '%s', RETRIES_ = %d, TIMESTAMP_ = %d WHERE KEY_ = %d;";

  private static final String INSERT_MESSAGE =
      "INSERT INTO MESSAGE"
          + " (ID_, KEY_, NAME_, CORRELATION_KEY_, MESSAGE_ID_, PAYLOAD_, STATE_, TIMESTAMP_)"
          + " VALUES "
          + "('%s', %d, '%s', '%s', '%s', '%s', '%s', %d)";

  private static final String UPDATE_MESSAGE =
      "UPDATE MESSAGE SET STATE_ = '%s', TIMESTAMP_ = %d WHERE KEY_ = %d;";

  private static final String INSERT_MESSAGE_SUBSCRIPTION =
      "INSERT INTO MESSAGE_SUBSCRIPTION"
          + " (ID_, WORKFLOW_INSTANCE_KEY_, ELEMENT_INSTANCE_KEY_, MESSAGE_NAME_, CORRELATION_KEY_, STATE_, TIMESTAMP_)"
          + " VALUES "
          + "('%s', %d, %d, '%s', '%s', '%s', %d)";

  private static final String UPDATE_MESSAGE_SUBSCRIPTION =
      "UPDATE MESSAGE_SUBSCRIPTION SET STATE_ = '%s', TIMESTAMP_ = %d WHERE ELEMENT_INSTANCE_KEY_ = %d and MESSAGE_NAME_ = '%s';";

  private static final String INSERT_TIMER =
      "INSERT INTO TIMER"
          + " (ID_, KEY_, ELEMENT_INSTANCE_KEY_, HANDLER_NODE_ID_, DUE_DATE_, STATE_, TIMESTAMP_)"
          + " VALUES "
          + "('%s', %d, %d, '%s', %d, '%s', %d)";

  private static final String UPDATE_TIMER =
      "UPDATE TIMER SET STATE_ = '%s', TIMESTAMP_ = %d WHERE KEY_ = %d;";

  private static final String INSERT_WORKER =
      "INSERT INTO WORKER"
          + " (ID_, WORKER_, JOB_TYPE_, TIMESTAMP_)"
          + " VALUES "
          + "('%s', '%s', '%s', %d)";

  private static final String REMOVE_WORKER =
      "DELETE FROM WORKER WHERE WORKER_ = '%s' and JOB_TYPE_ = '%s' and TIMESTAMP_ < %d;";

  private static final String INSERT_VARIABLE =
      "INSERT INTO VARIABLE"
          + " (ID_, NAME_, VALUE_, WORKFLOW_INSTANCE_KEY_, SCOPE_KEY_, STATE_, TIMESTAMP_)"
          + " VALUES "
          + "('%s', '%s', '%s', %d, %d, '%s', %d)";

  public static final String CREATE_SCHEMA_SQL_PATH = "/CREATE_SCHEMA.sql";

  private final Map<ValueType, Consumer<Record>> insertCreatorPerType = new HashMap<>();
  private final List<String> sqlStatements;

  private Logger log;
  private Controller controller;
  private SimpleMonitorExporterConfiguration configuration;

  private Connection connection;
  private int batchSize;
  private int batchTimerMilli;
  private Duration batchExecutionTimer;
  private long lastPosition;

  public SimpleMonitorExporter() {
    insertCreatorPerType.put(ValueType.DEPLOYMENT, this::exportDeploymentRecord);
    insertCreatorPerType.put(ValueType.WORKFLOW_INSTANCE, this::exportWorkflowInstanceRecord);
    insertCreatorPerType.put(ValueType.INCIDENT, this::exportIncidentRecord);
    insertCreatorPerType.put(ValueType.JOB, this::exportJobRecord);
    insertCreatorPerType.put(ValueType.MESSAGE, this::exportMessageRecord);
    insertCreatorPerType.put(ValueType.MESSAGE_SUBSCRIPTION, this::exportMessageSubscriptionRecord);
    insertCreatorPerType.put(ValueType.TIMER, this::exportTimerRecord);
    insertCreatorPerType.put(ValueType.JOB_BATCH, this::exportJobBatchRecord);
    insertCreatorPerType.put(ValueType.VARIABLE, this::exportVariableRecord);

    sqlStatements = new ArrayList<>();
  }

  @Override
  public void configure(final Context context) {
    log = context.getLogger();
    configuration =
        context.getConfiguration().instantiate(SimpleMonitorExporterConfiguration.class);

    applyEnvironmentVariables(configuration);

    batchSize = configuration.batchSize;
    batchTimerMilli = configuration.batchTimerMilli;

    log.debug("Exporter configured with {}", configuration);
    try {
      Class.forName(configuration.driverName);
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException("Driver not found in class path", e);
    }

      context.setFilter(
              new Context.RecordFilter() {
                  @Override
                  public boolean acceptType(RecordType recordType) {
                      return recordType == RecordType.EVENT;
                  }

                  @Override
                  public boolean acceptValue(ValueType valueType) {
                      return insertCreatorPerType.containsKey(valueType);
                  }
              });
  }

  private void applyEnvironmentVariables(final SimpleMonitorExporterConfiguration configuration) {
    final Map<String, String> environment = System.getenv();

    Optional.ofNullable(environment.get(ENV_JDBC_URL))
        .ifPresent(url -> configuration.jdbcUrl = url);
    Optional.ofNullable(environment.get(ENV_JDBC_DRIVER))
        .ifPresent(driver -> configuration.driverName = driver);
    Optional.ofNullable(environment.get(ENV_JDBC_USER))
        .ifPresent(user -> configuration.userName = user);
    Optional.ofNullable(environment.get(ENV_JDBC_PASSWORD))
        .ifPresent(password -> configuration.password = password);
  }

  @Override
  public void open(final Controller controller) {
    try {
      connection =
          DriverManager.getConnection(
              configuration.jdbcUrl, configuration.userName, configuration.password);
      connection.setAutoCommit(true);
    } catch (final SQLException e) {
      throw new RuntimeException(
          String.format("Error on opening database with configuration %s.", configuration), e);
    }

    createTables();
    log.info("Start exporting to {}.", configuration.jdbcUrl);

    this.controller = controller;
    if (batchTimerMilli > 0) {
      batchExecutionTimer = Duration.ofMillis(batchTimerMilli);
      this.controller.scheduleTask(batchExecutionTimer, this::batchTimerExecution);
    }
  }

  private void createTables() {
    if (!configuration.createSchema) {
      return;
    }

    try (final Statement statement = connection.createStatement()) {

      final InputStream resourceAsStream =
          SimpleMonitorExporter.class.getResourceAsStream(CREATE_SCHEMA_SQL_PATH);
      final String sql =
          new BufferedReader(new InputStreamReader(resourceAsStream))
              .lines()
              .collect(Collectors.joining(System.lineSeparator()));

      log.debug("Create tables:\n{}", sql);
      statement.executeUpdate(sql);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    try {
      connection.close();
    } catch (final Exception e) {
      log.warn("Failed to close jdbc connection", e);
    }
    log.info("Exporter closed");
  }

  @Override
  public void export(final Record record) {
    lastPosition = record.getPosition();

    final Consumer<Record> recordConsumer = insertCreatorPerType.get(record.getValueType());
    if (recordConsumer != null) {
      recordConsumer.accept(record);

      if (sqlStatements.size() > batchSize) {
        executeSqlStatementBatch();
      }
    }
  }

  private void batchTimerExecution() {
    executeSqlStatementBatch();
    controller.scheduleTask(batchExecutionTimer, this::batchTimerExecution);
  }

  private void executeSqlStatementBatch() {
    try (final Statement statement = connection.createStatement()) {
      for (final String insert : sqlStatements) {
        statement.addBatch(insert);
      }
      statement.executeBatch();
      sqlStatements.clear();
    } catch (final Exception e) {
      log.error("Batch insert failed!", e);
    }
    controller.updateLastExportedRecordPosition(lastPosition);
  }

  private void exportDeploymentRecord(final Record<DeploymentRecordValue> record) {
    if (record.getIntent() != DeploymentIntent.CREATED
        || record.getPartitionId() != Protocol.DEPLOYMENT_PARTITION) {
      // ignore deployment event on other partitions to avoid duplicates
      return;
    }
    final long timestamp = record.getTimestamp();
    final DeploymentRecordValue deploymentRecordValue = (DeploymentRecordValue) record.getValue();

    final List<DeploymentResource> resources = deploymentRecordValue.getResources();
    for (final DeploymentResource resource : resources) {
      final List<DeployedWorkflow> deployedWorkflows =
          deploymentRecordValue.getDeployedWorkflows().stream()
              .filter(w -> w.getResourceName().equals(resource.getResourceName()))
              .collect(Collectors.toList());
      for (final DeployedWorkflow deployedWorkflow : deployedWorkflows) {
        final String insertStatement =
            String.format(
                INSERT_WORKFLOW,
                createId(),
                deployedWorkflow.getWorkflowKey(),
                getCleanString(deployedWorkflow.getBpmnProcessId()),
                deployedWorkflow.getVersion(),
                getCleanString(new String(resource.getResource())),
                timestamp);
        sqlStatements.add(insertStatement);
      }
    }
  }

  private boolean isWorkflowInstance(final Record<WorkflowInstanceRecordValue> record) {
    return record.getValue().getWorkflowInstanceKey() == record.getKey();
  }

  private void exportWorkflowInstanceRecord(final Record<WorkflowInstanceRecordValue> record) {
    final long key = record.getKey();
    final int partitionId = record.getPartitionId();
    final Intent intent = record.getIntent();
    final long timestamp = record.getTimestamp();

    final WorkflowInstanceRecordValue workflowInstanceRecordValue = record.getValue();

    if (isWorkflowInstance(record)) {
      exportWorkflowInstance(key, partitionId, intent, timestamp, workflowInstanceRecordValue);
    } else {
      exportElementInstance(key, partitionId, intent, timestamp, workflowInstanceRecordValue);
    }
  }

  private void exportWorkflowInstance(
      final long key,
      final int partitionId,
      final Intent intent,
      final long timestamp,
      final WorkflowInstanceRecordValue workflowInstanceRecordValue) {

    if (intent == WorkflowInstanceIntent.ELEMENT_ACTIVATED) {
      final String bpmnProcessId = getCleanString(workflowInstanceRecordValue.getBpmnProcessId());
      final int version = workflowInstanceRecordValue.getVersion();
      final long workflowKey = workflowInstanceRecordValue.getWorkflowKey();

      final String insertWorkflowInstanceStatement =
          String.format(
              INSERT_WORKFLOW_INSTANCE,
              createId(),
              partitionId,
              key,
              bpmnProcessId,
              version,
              workflowKey,
              "Active",
              timestamp);
      sqlStatements.add(insertWorkflowInstanceStatement);
    } else if (intent == WorkflowInstanceIntent.ELEMENT_COMPLETED) {
      final String updateWorkflowInstanceStatement =
          String.format(UPDATE_WORKFLOW_INSTANCE, timestamp, "Completed", key);
      sqlStatements.add(updateWorkflowInstanceStatement);
    } else if (intent == WorkflowInstanceIntent.ELEMENT_TERMINATED) {
      final String updateWorkflowInstanceStatement =
          String.format(UPDATE_WORKFLOW_INSTANCE, timestamp, "Terminated", key);
      sqlStatements.add(updateWorkflowInstanceStatement);
    }
  }

  private void exportElementInstance(
      final long key,
      final int partitionId,
      final Intent intent,
      final long timestamp,
      final WorkflowInstanceRecordValue workflowInstanceRecordValue) {
    final long workflowInstanceKey = workflowInstanceRecordValue.getWorkflowInstanceKey();
    final String elementId = getCleanString(workflowInstanceRecordValue.getElementId());
    final long flowScopeKey = workflowInstanceRecordValue.getFlowScopeKey();
    final long workflowKey = workflowInstanceRecordValue.getWorkflowKey();

    final String insertActivityInstanceStatement =
        String.format(
            INSERT_ELEMENT_INSTANCE,
            createId(),
            partitionId,
            key,
            intent,
            workflowInstanceKey,
            elementId,
            flowScopeKey,
            workflowKey,
            timestamp);
    sqlStatements.add(insertActivityInstanceStatement);
  }

  private void exportIncidentRecord(final Record<IncidentRecordValue> record) {
    final long key = record.getKey();
    final Intent intent = record.getIntent();
    final long timestamp = record.getTimestamp();

    final IncidentRecordValue incidentRecordValue = (IncidentRecordValue) record.getValue();
    final String bpmnProcessId = getCleanString(incidentRecordValue.getBpmnProcessId());
    final long workflowKey = incidentRecordValue.getWorkflowKey();
    final long workflowInstanceKey = incidentRecordValue.getWorkflowInstanceKey();
    final long elementInstanceKey = incidentRecordValue.getElementInstanceKey();
    final long jobKey = incidentRecordValue.getJobKey();
    final String errorType = getCleanString(incidentRecordValue.getErrorType().name());
    final String errorMessage = getCleanString(incidentRecordValue.getErrorMessage());

    if (intent == IncidentIntent.CREATED) {
      final String insertStatement =
          String.format(
              INSERT_INCIDENT,
              createId(),
              key,
              bpmnProcessId,
              workflowKey,
              workflowInstanceKey,
              elementInstanceKey,
              jobKey,
              errorType,
              errorMessage,
              timestamp);
      sqlStatements.add(insertStatement);
    } else if (intent == IncidentIntent.RESOLVED) {
      final String updateIncidentStatement = String.format(UPDATE_INCIDENT, timestamp, key);
      sqlStatements.add(updateIncidentStatement);
    }
  }

  private void exportJobRecord(final Record<JobRecordValue> record) {
    final long key = record.getKey();
    final Intent intent = record.getIntent();
    final long timestamp = record.getTimestamp();
    final String state = intent.name().toLowerCase();

    final JobRecordValue jobRecord = record.getValue();
    final String jobType = jobRecord.getType();
    final long workflowInstanceKey = jobRecord.getWorkflowInstanceKey();
    final long elementInstanceKey = jobRecord.getElementInstanceKey();
    final int retries = jobRecord.getRetries();
    final String worker = jobRecord.getWorker();

    if (intent == JobIntent.CREATED) {
      final String insertStatement =
          String.format(
              INSERT_JOB,
              createId(),
              key,
              jobType,
              workflowInstanceKey,
              elementInstanceKey,
              state,
              retries,
              timestamp);
      sqlStatements.add(insertStatement);
    } else {
      final String updateStatement =
          String.format(UPDATE_JOB, state, worker, retries, timestamp, key);
      sqlStatements.add(updateStatement);
    }
  }

  private void exportMessageRecord(final Record<MessageRecordValue> record) {
    final long key = record.getKey();
    final Intent intent = record.getIntent();
    final long timestamp = record.getTimestamp();
    final String state = intent.name().toLowerCase();

    final MessageRecordValue messageRecord = (MessageRecordValue) record.getValue();

    final String name = messageRecord.getName();
    final String correlationKey = messageRecord.getCorrelationKey();
    final String messageId = messageRecord.getMessageId();
    final String variables = messageRecord.getVariables().toString();

    if (intent == MessageIntent.PUBLISHED) {
      final String insertStatement =
          String.format(
              INSERT_MESSAGE,
              createId(),
              key,
              name,
              correlationKey,
              messageId,
              variables,
              state,
              timestamp);
      sqlStatements.add(insertStatement);
    } else {
      final String updateStatement = String.format(UPDATE_MESSAGE, state, timestamp, key);
      sqlStatements.add(updateStatement);
    }
  }

  private void exportMessageSubscriptionRecord(
      final Record<MessageSubscriptionRecordValue> record) {
    final Intent intent = record.getIntent();
    final long timestamp = record.getTimestamp();
    final String state = intent.name().toLowerCase();

    final MessageSubscriptionRecordValue subscriptionRecord = record.getValue();

    final String messageName = subscriptionRecord.getMessageName();
    final String correlationKey = subscriptionRecord.getCorrelationKey();
    final long workflowInstanceKey = subscriptionRecord.getWorkflowInstanceKey();
    final long elementInstanceKey = subscriptionRecord.getElementInstanceKey();

    if (intent == MessageSubscriptionIntent.OPENED) {
      final String insertStatement =
          String.format(
              INSERT_MESSAGE_SUBSCRIPTION,
              createId(),
              workflowInstanceKey,
              elementInstanceKey,
              messageName,
              correlationKey,
              state,
              timestamp);
      sqlStatements.add(insertStatement);
    } else {
      final String updateStatement =
          String.format(
              UPDATE_MESSAGE_SUBSCRIPTION, state, timestamp, elementInstanceKey, messageName);
      sqlStatements.add(updateStatement);
    }
  }

  private void exportTimerRecord(final Record<TimerRecordValue> record) {
    final long key = record.getKey();
    final Intent intent = record.getIntent();
    final long timestamp = record.getTimestamp();
    final String state = intent.name().toLowerCase();

    final TimerRecordValue timerRecord = (TimerRecordValue) record.getValue();

    final long elementInstanceKey = timerRecord.getElementInstanceKey();
      final String handlerNodeId = timerRecord.getTargetElementId();
    final long dueDate = timerRecord.getDueDate();

    if (intent == TimerIntent.CREATED) {
      final String insertStatement =
          String.format(
              INSERT_TIMER,
              createId(),
              key,
              elementInstanceKey,
              handlerNodeId,
              dueDate,
              state,
              timestamp);
      sqlStatements.add(insertStatement);
    } else {
      final String updateStatement = String.format(UPDATE_TIMER, state, timestamp, key);
      sqlStatements.add(updateStatement);
    }
  }

  private void exportJobBatchRecord(final Record<JobBatchRecordValue> record) {
    final Intent intent = record.getIntent();
    final long timestamp = record.getTimestamp();

    final JobBatchRecordValue jobBatch = record.getValue();

    final String worker = jobBatch.getWorker();
    final String jobType = jobBatch.getType();

    if (intent == JobBatchIntent.ACTIVATED) {
      final String insertStatement =
          String.format(INSERT_WORKER, createId(), worker, jobType, timestamp);
      sqlStatements.add(insertStatement);

      final String removeStatement = String.format(REMOVE_WORKER, worker, jobType, timestamp);
      sqlStatements.add(removeStatement);
    }
  }

  private void exportVariableRecord(final Record<VariableRecordValue> record) {
    final Intent intent = record.getIntent();
    final long timestamp = record.getTimestamp();
    final String state = intent.name().toLowerCase();

    final VariableRecordValue variableRecord = record.getValue();

    final String name = variableRecord.getName();
    final String value = variableRecord.getValue();
    final long workflowInstanceKey = variableRecord.getWorkflowInstanceKey();
    final long scopeKey = variableRecord.getScopeKey();

    final String insertStatement =
        String.format(
            INSERT_VARIABLE,
            createId(),
            name,
            value,
            workflowInstanceKey,
            scopeKey,
            state,
            timestamp);
    sqlStatements.add(insertStatement);
  }

  private String getCleanString(final String string) {
    return string.trim().replaceAll("'", "`");
  }

  private String createId() {
    return UUID.randomUUID().toString();
  }
}
