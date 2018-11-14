package io.zeebe.monitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.zeebe.exporter.context.Configuration;
import io.zeebe.exporter.context.Context;
import io.zeebe.exporter.context.Controller;
import io.zeebe.exporter.record.Record;
import io.zeebe.exporter.record.RecordMetadata;
import io.zeebe.exporter.record.value.DeploymentRecordValue;
import io.zeebe.exporter.record.value.WorkflowInstanceRecordValue;
import io.zeebe.exporter.record.value.deployment.DeployedWorkflow;
import io.zeebe.exporter.record.value.deployment.DeploymentResource;
import io.zeebe.exporter.record.value.deployment.ResourceType;
import io.zeebe.protocol.clientapi.RecordType;
import io.zeebe.protocol.clientapi.ValueType;
import io.zeebe.protocol.intent.DeploymentIntent;
import io.zeebe.protocol.intent.Intent;
import io.zeebe.protocol.intent.WorkflowInstanceIntent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleMonitorExporterTest {

  public static final String SELECT_FROM_WORKFLOW = "SELECT * FROM WORKFLOW;";
  public static final String SELECT_FROM_WORKFLOW_INSTANCE = "SELECT * FROM WORKFLOW_INSTANCE;";
  public static final String SELECT_FROM_ACTIVITY_INSTANCE = "SELECT * FROM ACTIVITY_INSTANCE;";

  private SimpleMonitorExporter exporter;
  private SimpleMonitorExporterConfiguration configuration;

  @Before
  public void setup() {
    exporter = new SimpleMonitorExporter();
    configuration = new SimpleMonitorExporterConfiguration();
    configuration.jdbcUrl = "jdbc:h2:mem:zeebe-monitor";
    configuration.batchSize = 0;

    final Context contextMock = mock(Context.class);
    final Configuration configMock = mock(Configuration.class);
    when(configMock.instantiate(SimpleMonitorExporterConfiguration.class))
        .thenReturn(configuration);
    when(contextMock.getConfiguration()).thenReturn(configMock);

    final Logger logger = LoggerFactory.getLogger("simple-monitor");
    when(contextMock.getLogger()).thenReturn(logger);

    exporter.configure(contextMock);

    final Controller controllerMock = mock(Controller.class);
    exporter.open(controllerMock);
  }

  @Test
  public void shouldCreateTables() throws Exception {
    // given

    // when
    try (final Connection connection =
        DriverManager.getConnection(
            configuration.jdbcUrl, configuration.userName, configuration.password)) {
      try (final Statement statement = connection.createStatement()) {
        statement.execute("SELECT * FROM WORKFLOW;");
        statement.execute("SELECT * FROM WORKFLOW_INSTANCE;");
        statement.execute("SELECT * FROM INCIDENT;");
      }
    }

    // then no error should happen
  }

  @Test
  public void shouldInsertWorkflow() throws Exception {
    // given
    final Record deploymentRecord =
        createRecordMockForIntent(ValueType.DEPLOYMENT, DeploymentIntent.CREATED);
    final DeploymentRecordValue deploymentRecordValueMock = createDeploymentRecordValueMock();
    when(deploymentRecord.getValue()).thenReturn(deploymentRecordValueMock);

    // when
    exporter.export(deploymentRecord);

    // then
    try (final Connection connection =
        DriverManager.getConnection(
            configuration.jdbcUrl, configuration.userName, configuration.password)) {
      try (final Statement statement = connection.createStatement()) {
        statement.execute(SELECT_FROM_WORKFLOW);
        final ResultSet resultSet = statement.getResultSet();
        resultSet.beforeFirst();
        resultSet.next();

        // (ID_, KEY_, BPMN_PROCESS_ID_, VERSION_, RESOURCE_, TIMESTAMP_)
        final String uuid = resultSet.getString(1);
        UUID.fromString(uuid); // should not thrown an exception

        final long key = resultSet.getLong(2);
        assertThat(key).isEqualTo(1);

        final String bpmnProcessId = resultSet.getString(3);
        assertThat(bpmnProcessId).isEqualTo("process");

        final int version = resultSet.getInt(4);
        assertThat(version).isEqualTo(0);

        final String resource = resultSet.getString(5);
        assertThat(resource).isEqualTo("ThisistheResource");

        final long timeStamp = resultSet.getLong(6);
        assertThat(timeStamp).isGreaterThan(0).isLessThan(Instant.now().toEpochMilli());
      }
    }
  }

  @Test
  public void shouldInsertWorkflowInstance() throws Exception {
    // given
    final Record workflowInstanceRecord =
        createRecordMockForIntent(
            ValueType.WORKFLOW_INSTANCE, WorkflowInstanceIntent.ELEMENT_ACTIVATED, 4L);
    final WorkflowInstanceRecordValue instanceRecordValueMock =
        createWorkflowInstanceRecordValueMock("process");
    when(workflowInstanceRecord.getValue()).thenReturn(instanceRecordValueMock);

    // when
    exporter.export(workflowInstanceRecord);

    // then
    try (final Connection connection =
        DriverManager.getConnection(
            configuration.jdbcUrl, configuration.userName, configuration.password)) {
      try (final Statement statement = connection.createStatement()) {
        statement.execute(SELECT_FROM_WORKFLOW_INSTANCE);
        final ResultSet resultSet = statement.getResultSet();
        resultSet.beforeFirst();
        resultSet.next();

        // ID_, PARTITION_ID_, KEY_, BPMN_PROCESS_ID_, VERSION_, WORKFLOW_KEY_, START_, END_
        final String uuid = resultSet.getString(1);
        UUID.fromString(uuid); // should not thrown an exception

        final int partitionId = resultSet.getInt(2);
        assertThat(partitionId).isEqualTo(0);

        final long key = resultSet.getLong(3);
        assertThat(key).isEqualTo(4L);

        final String bpmnProcessId = resultSet.getString(4);
        assertThat(bpmnProcessId).isEqualTo("process");

        final int version = resultSet.getInt(5);
        assertThat(version).isEqualTo(0);

        final long workflowKey = resultSet.getLong(6);
        assertThat(workflowKey).isEqualTo(1);

        final long start = resultSet.getLong(7);
        assertThat(start).isGreaterThan(0).isLessThan(Instant.now().toEpochMilli());

        final long end = resultSet.getLong(8);
        assertThat(end).isEqualTo(0);
      }
    }
  }

  @Test
  public void shouldUpdateWorkflowInstance() throws Exception {
    // given
    final Record workflowInstanceRecord =
        createRecordMockForIntent(
            ValueType.WORKFLOW_INSTANCE, WorkflowInstanceIntent.ELEMENT_ACTIVATED, 4L);
    final WorkflowInstanceRecordValue instanceRecordValueMock =
        createWorkflowInstanceRecordValueMock("process");
    when(workflowInstanceRecord.getValue()).thenReturn(instanceRecordValueMock);
    exporter.export(workflowInstanceRecord);

    final Record secondRecord =
        createRecordMockForIntent(
            ValueType.WORKFLOW_INSTANCE, WorkflowInstanceIntent.ELEMENT_TERMINATED, 4L);
    final WorkflowInstanceRecordValue secondValueMock =
        createWorkflowInstanceRecordValueMock("process");
    when(secondRecord.getValue()).thenReturn(secondValueMock);

    // when
    exporter.export(secondRecord);

    // then
    try (final Connection connection =
        DriverManager.getConnection(
            configuration.jdbcUrl, configuration.userName, configuration.password)) {
      try (final Statement statement = connection.createStatement()) {
        statement.execute(SELECT_FROM_WORKFLOW_INSTANCE);
        final ResultSet resultSet = statement.getResultSet();
        resultSet.beforeFirst();
        resultSet.next();

        // ID_, PARTITION_ID_, KEY_, BPMN_PROCESS_ID_, VERSION_, WORKFLOW_KEY_, START_, END_
        final String uuid = resultSet.getString(1);
        UUID.fromString(uuid); // should not thrown an exception

        final int partitionId = resultSet.getInt(2);
        assertThat(partitionId).isEqualTo(0);

        final long key = resultSet.getLong(3);
        assertThat(key).isEqualTo(4L);

        final String bpmnProcessId = resultSet.getString(4);
        assertThat(bpmnProcessId).isEqualTo("process");

        final int version = resultSet.getInt(5);
        assertThat(version).isEqualTo(0);

        final long workflowKey = resultSet.getLong(6);
        assertThat(workflowKey).isEqualTo(1);

        final long start = resultSet.getLong(7);
        assertThat(start).isGreaterThan(0).isLessThan(Instant.now().toEpochMilli());

        final long end = resultSet.getLong(8);
        assertThat(end).isGreaterThan(start);
      }
    }
  }

  @Test
  public void shouldCreateActivityInstance() throws Exception {
    // given
    final Record workflowInstanceRecord =
        createRecordMockForIntent(
            ValueType.WORKFLOW_INSTANCE, WorkflowInstanceIntent.SEQUENCE_FLOW_TAKEN);
    final WorkflowInstanceRecordValue instanceRecordValueMock =
        createWorkflowInstanceRecordValueMock("sequenceFlow");
    when(workflowInstanceRecord.getValue()).thenReturn(instanceRecordValueMock);
    exporter.export(workflowInstanceRecord);

    // when
    exporter.export(workflowInstanceRecord);

    // then
    try (final Connection connection =
        DriverManager.getConnection(
            configuration.jdbcUrl, configuration.userName, configuration.password)) {
      try (final Statement statement = connection.createStatement()) {
        statement.execute(SELECT_FROM_ACTIVITY_INSTANCE);
        final ResultSet resultSet = statement.getResultSet();
        resultSet.beforeFirst();
        resultSet.next();

        // ID_, PARTITION_ID_, KEY_, INTENT, WORKFLOW_INSTANCE_KEY_, ACTIVITY_ID,
        // SCOPE_INSTANCE_KEY, PAYLOAD, WORKFLOW_KEY, TIMESTAMP
        final String uuid = resultSet.getString(1);
        UUID.fromString(uuid); // should not thrown an exception

        final int partitionId = resultSet.getInt(2);
        assertThat(partitionId).isEqualTo(0);

        final long key = resultSet.getLong(3);
        assertThat(key).isEqualTo(1L);

        final String intent = resultSet.getString(4);
        assertThat(intent).isEqualTo("SEQUENCE_FLOW_TAKEN");

        final int workflowInstanceKey = resultSet.getInt(5);
        assertThat(workflowInstanceKey).isEqualTo(4L);

        final String activityId = resultSet.getString(6);
        assertThat(activityId).isEqualTo("sequenceFlow");

        final long scopeInstanceKey = resultSet.getLong(7);
        assertThat(scopeInstanceKey).isEqualTo(-1);

        final String payload = resultSet.getString(8);
        assertThat(payload).isEqualTo("{\"foo\":\"bar\"}");

        final long workflowKey = resultSet.getLong(9);
        assertThat(workflowKey).isEqualTo(1L);

        final long timestamp = resultSet.getLong(10);
        assertThat(timestamp).isGreaterThan(0).isLessThan(Instant.now().toEpochMilli());
      }
    }
  }

  private Record createRecordMockForIntent(final ValueType valueType, final Intent intent) {
    return createRecordMockForIntent(valueType, intent, 1L);
  }

  private Record createRecordMockForIntent(
      final ValueType valueType, final Intent intent, final long key) {
    final Record recordMock = mock(Record.class);
    when(recordMock.getKey()).thenReturn(key);
    when(recordMock.getTimestamp()).thenReturn(Instant.now());

    final RecordMetadata metadataMock = mock(RecordMetadata.class);
    when(metadataMock.getRecordType()).thenReturn(RecordType.EVENT);
    when(metadataMock.getPartitionId()).thenReturn(0);
    when(metadataMock.getValueType()).thenReturn(valueType);
    when(metadataMock.getIntent()).thenReturn(intent);

    when(recordMock.getMetadata()).thenReturn(metadataMock);
    return recordMock;
  }

  private DeploymentRecordValue createDeploymentRecordValueMock() {
    final DeploymentRecordValue deploymentRecordValueMock = mock(DeploymentRecordValue.class);

    final DeploymentResource resourceMock = mock(DeploymentResource.class);
    when(resourceMock.getResource()).thenReturn("ThisistheResource".getBytes());
    when(resourceMock.getResourceName()).thenReturn("process.bpmn");
    when(resourceMock.getResourceType()).thenReturn(ResourceType.BPMN_XML);

    final List<DeploymentResource> resourceList = new ArrayList<>();
    resourceList.add(resourceMock);
    when(deploymentRecordValueMock.getResources()).thenReturn(resourceList);

    final DeployedWorkflow deployedWorkflowMock = mock(DeployedWorkflow.class);
    when(deployedWorkflowMock.getResourceName()).thenReturn("process.bpmn");
    when(deployedWorkflowMock.getBpmnProcessId()).thenReturn("process");
    when(deployedWorkflowMock.getVersion()).thenReturn(0);
    when(deployedWorkflowMock.getWorkflowKey()).thenReturn(1L);

    final List<DeployedWorkflow> deployedWorkflows = new ArrayList<>();
    deployedWorkflows.add(deployedWorkflowMock);
    when(deploymentRecordValueMock.getDeployedWorkflows()).thenReturn(deployedWorkflows);

    return deploymentRecordValueMock;
  }

  private WorkflowInstanceRecordValue createWorkflowInstanceRecordValueMock(
      final String elementId) {
    final WorkflowInstanceRecordValue instanceRecordValue = mock(WorkflowInstanceRecordValue.class);

    when(instanceRecordValue.getElementId()).thenReturn(elementId);
    when(instanceRecordValue.getScopeInstanceKey()).thenReturn(-1L);
    when(instanceRecordValue.getWorkflowInstanceKey()).thenReturn(4L);
    when(instanceRecordValue.getPayload()).thenReturn("{\"foo\":\"bar\"}");

    when(instanceRecordValue.getWorkflowKey()).thenReturn(1L);
    when(instanceRecordValue.getVersion()).thenReturn(0);
    when(instanceRecordValue.getBpmnProcessId()).thenReturn("process");
    return instanceRecordValue;
  }
}
