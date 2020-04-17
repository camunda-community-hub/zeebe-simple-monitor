Zeebe Simple Monitor
=========================

A monitoring application for [Zeebe](https://zeebe.io). It is designed for developers to

* get in touch with Zeebe and workflow execution (BPMN)
* test workflows manually
* provide insides how workflows are executed 

The application imports the data from Zeebe using the [Hazelcast exporter](https://github.com/zeebe-io/zeebe-hazelcast-exporter). It aggregates the data and store it into a (in-memory) database. The data is display on server-side rendered HTML pages.

![how-it-works](docs/how-it-works.png)

## Install

### Docker

The docker image for the worker is published to [DockerHub](https://hub.docker.com/r/camunda/zeebe-simple-monitor).

```
docker pull camunda/zeebe-simple-monitor:latest
```

* ensure that a Zeebe broker is running with a Hazelcast exporter (>= 0.8.0-alpha1)  
* forward the Hazelcast port to the docker container (default: `5701`)
* configure the connection to the Zeebe broker by setting `zeebe.client.broker.contactPoint` (default: `localhost:26500`) 
* configure the connection to Hazelcast by setting `zeebe.worker.hazelcast.connection` (default: `localhost:5701`) 

For a local setup, the repository contains a [docker-compose file](docker/docker-compose.yml). It starts a Zeebe broker with the Hazelcast exporter and the application. 

```
mvn clean install -DskipTests
cd docker
docker-compose up
```

Go to http://localhost:8082

### Manual

1. Download the latest [application JAR](https://github.com/zeebe-io/zeebe-simple-monitor/releases) _(zeebe-simple-monitor-%{VERSION}.jar
)_

1. Start the application
	`java -jar zeebe-simple-monitor-{VERSION}.jar`

1. Go to http://localhost:8082

### Configuration

The worker is a Spring Boot application that uses the [Spring Zeebe Starter](https://github.com/zeebe-io/spring-zeebe). The configuration can be changed via environment variables or an `application.yaml` file. See also the following resources:
* [Spring Zeebe Configuration](https://github.com/zeebe-io/spring-zeebe#configuring-zeebe-connection)
* [Spring Boot Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config)

By default, the port is set to `8082` and the database is only in-memory (i.e. not persistent).

```
zeebe:

  worker:
    hazelcast:
      connection: localhost:5701

  client:
    broker.contactPoint: 127.0.0.1:26500
    security.plaintext: true

spring:

  datasource:
    url: jdbc:h2:mem:zeebe-monitor;DB_CLOSE_DELAY=-1
    user: sa
    password:
    driverClassName: org.h2.Driver

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update

server:
  port: 8082
```

## Build from Source

Build with Maven
   
`mvn clean install`

## Code of Conduct

This project adheres to the Contributor Covenant [Code of
Conduct](/CODE_OF_CONDUCT.md). By participating, you are expected to uphold
this code. Please report unacceptable behavior to code-of-conduct@zeebe.io.

## License

[Apache License, Version 2.0](/LICENSE)

## About

![screencast](docs/zeebe-simple-monitor.gif)
