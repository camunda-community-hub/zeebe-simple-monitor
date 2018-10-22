Zeebe Simple Monitor - Exporter
=========================

The current implementation is created to be used with the simple-monitor, but
you can adjust the implementation to your needs. It should be possible to use also other
relation databases instead of h2. If you have any problems please open an issue, write in the forum or on slack.

For more information about the exporters please read the [Exporter documentation](https://docs.zeebe.io/basics/exporters.html).

## How to export records

### Latest Zeebe Broker

Make sure you have downloaded and installed the latest Zeebe distribution.Please follow the [installation guide](https://docs.zeebe.io/introduction/install.html) if you need help.

### Copy the exporter

After that you need to build the exporter or use the latest released version. To build the exporter simply use `mvn clean install`.

Copy from the target folder the jar with dependencies into the lib folder of the broker.

```
cp target/zeebe-simple-monitor-exporter-%{VERSION}.jar ~/zeebe-broker-%{VERSION}/lib/
```

### Configure the Broker

After that you need to configure the Zeebe broker, so the broker starts the SimpleMonitorExporter. To that open the Zeebe configuration file `config/zeebe.cfg.toml`.

To enable the exporter add the following to the end of the file:

```
[[exporters]]
id = "simple-monitor"
className = "io.zeebe.monitor.SimpleMonitorExporter"
```

### Configure the Exporter

The jdbc uses some default properties which can be configured as well.
To do this you can add the following. All values are commeted out and have the default values. To change them remove the `#` and change the value.

```
[[exporters]]
id = "simple-monitor"
className = "io.zeebe.monitor.SimpleMonitorExporter"

 [exporters.args]
  #jdbcUrl = "jdbc:h2:~/zeebe-monitor;AUTO_SERVER=TRUE"

  # The driver name of the jdbc driver implementation. Make sure that the implementation is
  # available in the exporter/broker classpath (add it to the broker lib folder).
  # The name is used to load the driver implementation like this
  # Class.forName(configuration.driverName);
  # 
  #driverName = "org.h2.Driver"
  #userName = "sa"
  #password = ""

  # To configure the amount of records, which has to be reached before the records are exported to
  # the database. Only counts the records which are in the end actually exported.
  #
  # batchSize = 100;

  # To configure the time in milliseconds, when the batch should be executed regardless whether the
  # batch size was reached or not.
  #
  #If the value is less then one, then no timer will be scheduled.
  #
  #batchTimerMilli = 1000
```

### Start the broker

After configure the broker and also maybe configuring the exporter you are done and can start the broker.


In the broker log there should something like this been logged:

```
08:56:48.328 [exporter] [0.0.0.0:26501-zb-actors-1] INFO  io.zeebe.broker.exporter.simple-monitor - Create tables:
DROP INDEX IF EXISTS WORKFLOW_KEY_INDEX;
DROP INDEX IF EXISTS WORKFLOW_INSTANCE_WORKFLOW_KEY_INDEX;
DROP INDEX IF EXISTS ACTIVITY_WORKFLOW_INSTANCE_KEY_INDEX;
DROP INDEX IF EXISTS INCIDENT_WORKFLOW_INSTANCE_KEY_INDEX;

CREATE TABLE IF NOT EXISTS WORKFLOW
(
  ID_ VARCHAR PRIMARY KEY,
  KEY_ BIGINT,
  BPMN_PROCESS_ID_ VARCHAR,
  VERSION_ INT,
  RESOURCE_ VARCHAR,
  TIMESTAMP_ BIGINT
);

CREATE INDEX WORKFLOW_KEY_INDEX ON WORKFLOW(KEY_) ;

CREATE TABLE IF NOT EXISTS WORKFLOW_INSTANCE
(
        ID_ VARCHAR PRIMARY KEY,
        PARTITION_ID_ INT,
        KEY_ BIGINT,
  BPMN_PROCESS_ID_ VARCHAR,
  VERSION_ INT,
        WORKFLOW_KEY_ BIGINT,
  START_ BIGINT,
  END_ BIGINT
);

CREATE INDEX WORKFLOW_INSTANCE_WORKFLOW_KEY_INDEX ON WORKFLOW_INSTANCE(WORKFLOW_KEY_);

CREATE TABLE IF NOT EXISTS ACTIVITY_INSTANCE
(
        ID_ VARCHAR PRIMARY KEY,
        PARTITION_ID_ INT,
        KEY_ BIGINT,
        INTENT_ VARCHAR,
        WORKFLOW_INSTANCE_KEY_ BIGINT,
        ACTIVITY_ID_ VARCHAR,
        SCOPE_INSTANCE_KEY_ BIGINT,
        PAYLOAD_ VARCHAR,
        WORKFLOW_KEY_ BIGINT,
  TIMESTAMP_ BIGINT
);

CREATE INDEX ACTIVITY_WORKFLOW_INSTANCE_KEY_INDEX ON ACTIVITY_INSTANCE(WORKFLOW_INSTANCE_KEY_);

CREATE TABLE IF NOT EXISTS INCIDENT
(
        ID_ VARCHAR PRIMARY KEY,
        KEY_ BIGINT,
        INTENT_ VARCHAR,
        WORKFLOW_INSTANCE_KEY_ BIGINT,
        ACTIVITY_INSTANCE_KEY_ BIGINT,
        JOB_KEY_ BIGINT,
        ERROR_TYPE_ VARCHAR,
        ERROR_MSG_ VARCHAR,
  TIMESTAMP_ BIGINT
);

CREATE INDEX INCIDENT_WORKFLOW_INSTANCE_KEY_INDEX ON INCIDENT(WORKFLOW_INSTANCE_KEY_);
08:56:48.532 [exporter] [0.0.0.0:26501-zb-actors-1] INFO  io.zeebe.broker.exporter.simple-monitor - Start exporting to jdbc:h2:~/zeebe-monitor;AUTO_SERVER=TRUE.
```


If you saw this in the log everything worked.
Now you need simply create new events and reach the batch size, so the exporter exports these records or wait until the timer exceeeds. Then you can see the exported values in your database.

