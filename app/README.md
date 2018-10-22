Zeebe Simple Monitor - WebApp
=========================

The web application is built as an Spring Boot application.

### Configuration

The configuration can be provided as `applications.properties` or `applications.yaml` file or as environment variables.

In the configuration, you can change the connection to the database and to the Zeebe broker. The following lines show the default configuration:

```
spring.datasource.url=jdbc:h2:~/zeebe-monitor;AUTO_SERVER=TRUE
spring.datasource.user=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=validate

io.zeebe.monitor.connectionString=localhost:26500

logging.level.io.zeebe.zeebemonitor=DEBUG
```