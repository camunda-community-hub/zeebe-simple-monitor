package io.zeebe.monitor;

public class SimpleMonitorExporterConfiguration {

  String jdbcUrl = "jdbc:h2:~/zeebe-monitor;AUTO_SERVER=TRUE";

  /**
   * The driver name of the jdbc driver implementation. Make sure that the implementation is
   * available in the exporter/broker classpath (add it to the broker lib folder).
   *
   * <p>The name is used to load the driver implementation like this
   * Class.forName(configuration.driverName);
   */
  String driverName = "org.h2.Driver";

  String userName = "sa";

  String password = "";

  /**
   * To configure the amount of records, which has to be reached before the records are exported to
   * the database. Only counts the records which are in the end actually exported.
   */
  int batchSize = 100;

  /**
   * To configure the time in milliseconds, when the batch should be executed regardless whether the
   * batch size was reached or not.
   *
   * <p>If the value is less then one, then no timer will be scheduled.
   */
  int batchTimerMilli = 1000;

  /**
   * By default we are creating schema tables. Specify createSchema = false to disable table creation and assume they already exist.
   */
  boolean createSchema = true;

  @Override
  public String toString() {
    return "SimpleMonitorExporterConfiguration{"
        + "jdbcUrl='"
        + jdbcUrl
        + '\''
        + ", driverName='"
        + driverName
        + '\''
        + ", batchSize="
        + batchSize
        + ", batchTimerMilli="
        + batchTimerMilli
        + ", createSchema="
        + createSchema
        + '}';
  }
}
