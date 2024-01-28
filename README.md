Zeebe Simple Monitor
=========================

[![](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
[![](https://img.shields.io/badge/Lifecycle-Stable-brightgreen)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#stable-)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)](https://github.com/camunda-community-hub/community/blob/main/extension-lifecycle.md#compatiblilty)
[![](https://img.shields.io/badge/Maintainer%20Wanted-This%20extension%20is%20in%20search%20of%20a%20Maintainer-ff69b4)](https://github.com/camunda-community-hub/community/blob/main/extension-lifecycle.md)

A monitoring application for [Zeebe](https://zeebe.io). It is designed for developers to

* get in touch with Zeebe and workflow execution (BPMN)
* test workflows manually
* provide insides on how workflows are executed 

The application imports the data from Zeebe using the [Hazelcast exporter](https://github.com/camunda-community-hub/zeebe-hazelcast-exporter), [Kafka exporter](https://github.com/camunda-community-hub/zeebe-kafka-exporter) or [Redis exporter](https://github.com/camunda-community-hub/zeebe-redis-exporter). It aggregates the data and stores it into a database. The data is displayed on server-side rendered HTML pages.

![how-it-works](docs/how-it-works.png)

## Install

### Upgrading from a prior version

See the [upgrade instructions](./UPGRADE.md).

### Docker

The docker image for the worker is published to [GitHub Packages](https://github.com/orgs/camunda-community-hub/packages/container/package/zeebe-simple-monitor).

```
docker pull ghcr.io/camunda-community-hub/zeebe-simple-monitor:2.4.1
```

* ensure that a Zeebe broker is running with a [Hazelcast exporter](https://github.com/camunda-community-hub/zeebe-hazelcast-exporter#install) (>= `1.0.0`)  
* configure the connection to the Zeebe broker by setting `zeebe.client.broker.gateway-address` (default: `localhost:26500`) 
* configure the connection to Hazelcast by setting `zeebe.client.worker.hazelcast.connection` (default: `localhost:5701`)
* forward the Hazelcast port to the docker container (default: `5701`)
* if you want to set the Hazelcast clusterName then you need to adjust the Zeebe broker and the Zeebe Simple Monitor alike
  * Hint: this is useful, e.g. when you want to adjust the ringbuffer's size in the Hazelcast cluster (the name is relevant) 
  * a) in Zeebe broker, set the environment variable `ZEEBE_HAZELCAST_CLUSTER_NAME=dev` (default: `dev`)
  * b) in Zeebe Simple Monitor, change the setting `zeebe.client.worker.hazelcast.clusterName` (default: `dev`)

**Switch to the Kafka exporter/importer**

By default, the Zeebe Simple Monitor imports Zeebe events through Hazelcast, but you can switch to Kafka.

* Ensure that a Zeebe broker is running with a [Kafka exporter](https://github.com/camunda-community-hub/zeebe-kafka-exporter) (>= `3.1.1`)
* Configure the environment variables in the Zeebe broker:
  * Add spring configuration for the [zeebe-kafka-exporter](https://github.com/camunda-community-hub/zeebe-kafka-exporter): `SPRING_CONFIG_ADDITIONAL_LOCATION: /usr/local/zeebe/config/exporter.yml`. [Example](docker/kafka/exporter.yml) and [details](https://github.com/camunda-community-hub/zeebe-kafka-exporter?tab=readme-ov-file#configuration)
  * Inject `exporter.yml` and `zeebe-kafka-exporter.jar` into the Docker container, for example, using Docker Compose:
  ```
    volumes:
      - ./exporter.yml:/usr/local/zeebe/config/exporter.yml
      - ./zeebe-kafka-exporter-3.1.1-jar-with-dependencies.jar:/usr/local/zeebe/lib/zeebe-kafka-exporter.jar
  ```
  * Set the Kafka internal host: `KAFKA_BOOTSTRAP_SERVERS: "kafka:9092"`
  * Set the Kafka topic: `KAFKA_TOPIC: zeebe`
  * In order to import events efficiently and quickly, Zeebe brokers partitions and Kafka topic partitions should be correlated in a special way: [reference to the exporter docs](https://github.com/camunda-community-hub/zeebe-kafka-exporter?tab=readme-ov-file#partitioning)
* Configure the environment variables in the Zeebe Simple Monitor as described in the "[Change the default Zeebe importer to Kafka](#change-the-default-zeebe-importer-to-kafka)" section

**Switch to the Redis exporter/importer**

* Ensure that a Zeebe broker is running with a [Redis exporter](https://github.com/camunda-community-hub/zeebe-redis-exporter)
* Adjust the following environment variables in Zeebe:
  ```
  - ZEEBE_REDIS_REMOTE_ADDRESS=redis://redis:6379
  - ZEEBE_REDIS_MAX_TIME_TO_LIVE_IN_SECONDS=900
  - ZEEBE_REDIS_DELETE_AFTER_ACKNOWLEDGE=true
  ```
* Configure the connection to the Zeebe broker by setting `zeebe.client.broker.gateway-address` (default: `localhost:26500`)
* Configure the connection to Redis by setting `zeebe.client.worker.redis.connection` (default: `redis://localhost:6379`)
* Activate Redis by setting `zeebe-importer: redis`


If the Zeebe broker runs on your local machine with the default configs then start the container with the following command:  

```
docker run --network="host" ghcr.io/camunda-community-hub/zeebe-simple-monitor:2.4.1
```

For a local setup, the repository contains a [docker-compose file](docker/docker-compose.yml). It starts a Zeebe broker with the Hazelcast/Kafka/Redis exporter and the application. 
There are several Docker Compose profiles, setting by a file [.env](docker/.env), by passing multiple --profile flags or a comma-separated list for the COMPOSE_PROFILES environment variable:
* ```docker compose --profile hazelcast --profile hazelcast_in_memory up```
* ```COMPOSE_PROFILES=hazelcast,hazelcast_in_memory docker compose up```

Existing presets:
* ```COMPOSE_PROFILES=hazelcast,hazelcast_in_memory``` (by default)
* ```COMPOSE_PROFILES=kafka,kafka_in_memory```
* ```COMPOSE_PROFILES=redis,redis_in_memory```
* ```COMPOSE_PROFILES=hazelcast,hazelcast_postgres,postgres```
* ```COMPOSE_PROFILES=hazelcast,hazelcast_mysql,mysql```

The commands to build and run:
```
mvn clean install -DskipTests
cd docker
docker-compose up
```

Go to http://localhost:8082

To change the database see "[Change the Database](#change-the-database)"

To change Zeebe importer see "[Change the default Zeebe importer to Kafka](#change-the-default-zeebe-importer-to-kafka)"
or "[Change the default Zeebe importer to Redis](#change-the-default-zeebe-importer-to-redis)"

```
docker-compose --profile postgres up
```

### Manual

1. Download the latest [application JAR](https://github.com/zeebe-io/zeebe-simple-monitor/releases) _(zeebe-simple-monitor-%{VERSION}.jar
)_

1. Start the application
	`java -jar zeebe-simple-monitor-{VERSION}.jar`

1. Go to http://localhost:8082

### Configuration

The application is a Spring Boot application that uses the [Spring Zeebe Starter](https://github.com/zeebe-io/spring-zeebe). The configuration can be changed via environment variables or an `application.yaml` file. See also the following resources:
* [Spring Zeebe Configuration](https://github.com/zeebe-io/spring-zeebe#configuring-zeebe-connection)
* [Spring Boot Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config)

By default, the port is set to `8082` and the database is only in-memory (i.e. not persistent).

```
zeebe:

  client:
    broker.gateway-address: 127.0.0.1:26500
    security.plaintext: true
    
    worker:
      hazelcast:
        connection: localhost:5701
        clusterName: dev
        connectionTimeout: PT30S

# Options: hazelcast | kafka
# This config switches importers between the provided
# To use each of them, zeebe must be configured using hazelcast-exporter or kafka-exporter, respectively
# See the examples in docker/docker-compose.yml in services.zeebe and services.zeebe-kafka
zeebe-importer: hazelcast

spring:

  datasource:
    url: jdbc:h2:mem:zeebe-monitor;DB_CLOSE_DELAY=-1
    username: sa
    password:
    driverClassName: org.h2.Driver

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update

  kafka:
    template:
      default-topic: zeebe
    bootstrap-servers: localhost:9093
    properties:
      request.timeout.ms: 20000
      retry.backoff.ms: 500
    group-id: zeebe-simple-monitor
    consumer:
      auto-offset-reset: earliest
      properties:
        # 1Mb (1*1024*1024), max size of batch
        max.partition.fetch.bytes: 1048576
        # Number of messages in batch received by kafka listener.
        # Works only if their size is less than 'max.partition.fetch.bytes'
        max.poll.records: 1000
    custom:
      # Set equal to number of topic partitions to handle them in parallel
      concurrency: 3
      retry:
        intervalMs: 30000
        max-attempts: 3

server:
  port: 8082
  servlet:
    context-path: /
  allowedOriginsUrls: ""
```

#### Change the Context-Path

The context-path or base-path of the application can be changed using the following property:

``` 
server:
  servlet:
    context-path: /monitor/
```

It is then available under http://localhost:8082/monitor.

#### Cross Origin Requests

To enable Simple Monitor to send CORS header with every HTTP response,
add the allowed origins (`;` separated) in the following property:

``` 
server:
  allowedOriginsUrls: http://localhost:8082;https://monitor.cloud-provider.io:8082
```

This will then set ```Access-Control-Allow-Origin``` headers in every HTTP response.

#### Customize the Look & Feel

You can customize the look & feel of the Zeebe Simple Monitor (aka. white-labeling). For example, to change the logo or
alter the background color. The following configurations are available:

```
- white-label.logo.path=img/logo.png
- white-label.custom.title=Zeebe Simple Monitor
- white-label.custom.css.path=css/custom.css
- white-label.custom.js.path=js/custom.js
```

#### Change the Database

For example, using PostgreSQL:

* change the following database configuration settings

```
- spring.datasource.url=jdbc:postgresql://db:5432/postgres
- spring.datasource.username=postgres
- spring.datasource.password=zeebe
- spring.datasource.driverClassName=org.postgresql.Driver
- spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

* the PostgreSQL database driver is already bundled 

See the [docker-compose file](docker/docker-compose.yml) for a sample configuration with PostgreSQL. Profiles presets: `hazelcast,hazelcast_postgres,postgres`

The configuration for using MySql is similar but with an additional setting for the Hibernate naming strategy:

```
- spring.datasource.url=jdbc:mysql://db:3306/simple_monitor
- spring.datasource.username=root
- spring.datasource.password=zeebe
- spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
- spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
- spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
```

* the MySql database driver is already bundled

See the [docker-compose file](docker/docker-compose.yml) for a sample configuration with MySql. Profiles presets: `hazelcast,hazelcast_mysql,mysql`

#### Change the default Zeebe importer to Kafka

* set the `zeebe-importer` (default: `hazelcast`) configuration property to `kafka`
* configure the connection to Kafka by setting `spring.kafka.bootstrap-servers` (default: `localhost:9093`) 
* configure the Kafka topic by setting `spring.kafka.template.default-topic` (default: `zeebe`) 
* configure custom Kafka properties if necessary:
  * `spring.kafka.custom.concurrency` (default: `3`) is the number of threads for the Kafka listener that will import events from Zeebe
  * `spring.kafka.custom.retry.intervalMs` (default: `30000`)  and `spring.kafka.custom.retry.max-attempts` (default: `3`) are the retry configurations for a retryable exception in the listener

Refer to the [docker-compose file](docker/docker-compose.yml) for a sample configuration with the Kafka importer. Profile presets: `kafka,kafka_in_memory`

#### Change the default Zeebe importer to Redis

* set the `zeebe-importer` (default: `hazelcast`) configuration property to `redis`
* adjust the importer settings under `zeebe.client.worker.redis` (complete default values below):

```
zeebe:
  client:
    broker.gatewayAddress: 127.0.0.1:26500
    security.plaintext: true

    worker:
      redis:
        connection: redis://localhost:6379
        consumer-group: simple-monitor
        xread-count: 500
        xread-block-millis: 2000

zeebe-importer: redis
```

Refer to the [docker-compose file](docker/docker-compose.yml) for a sample configuration with the Redis importer. Profile presets: `redis,redis_in_memory`

## Code of Conduct

This project adheres to the Contributor Covenant [Code of
Conduct](/CODE_OF_CONDUCT.md). By participating, you are expected to uphold
this code. Please report unacceptable behavior to code-of-conduct@zeebe.io.

## License

[Apache License, Version 2.0](/LICENSE)

## About

![screencast](docs/zeebe-simple-monitor.gif)
