# Contributing to Zeebe-Simple-Monitor

:tada: First off, thanks for taking the time to contribute! :+1:

## How Can I Contribute?

### Reporting Bugs

If you found a bug or an unexpected behevior then please create
a [new issue](https://github.com/camunda-community-hub/zeebe-simple-monitor/issues). Before creating an issue, make sure
that there is no issue yet. Any information you provide in the issue, helps to solve it.

### Suggesting Enhancements

If you have an idea how to improve the project then please create
a [new issue](https://github.com/camunda-community-hub/zeebe-simple-monitor/issues). Describe your idea and the
motivation behind it.

Please note that this is a community-driven project. The maintainers may have not much time to implement new features if
they don't benefit directly from it. So, think about providing a pull request.

### Providing Pull Requests

You want to provide a bug fix or an improvement? Great! :tada:

Before opening a pull request, make sure that there is a related issue. The issue helps to confirm that the behavior is
unexpected, or the idea of the improvement is valid. (Following the rule "Talk, then code")

In order to verify that you don't break anything, you should build the whole project and run all tests. This also apply
the code formatting.

Please note that this is a community-driven project. The maintainers may have no time to review your pull request
immediately. Stay patient!

## Building the Project from Source

You can build the project with [Maven](http://maven.apache.org).

In the root directory:

Run the tests with

```
mvn test
```

Build the JAR files with

```
mvn clean install
```

## Styleguides

### Source Code

The Java code should be formatted using [Google's Java Format](https://github.com/google/google-java-format).

### Git Commit Messages

Commit messages should follow the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/#summary) format.

For example:

```
feat(ui): show deployed processes

* show all processes in a list view
* add navigation to the process view
```

Available commit types:

* `feat` - enhancements, new features
* `fix` - bug fixes
* `refactor` - non-behavior changes
* `test` - only changes in tests
* `docs` - changes in the documentation, readme, etc.
* `style` - apply code styles
* `build` - changes to the build (e.g. to Maven's `pom.xml`)
* `ci` - changes to the CI (e.g. to GitHub related configs)

## Hints for Developers

### Zeebe cluster backend for local development

There's a file [docker-compose.yml](docker/docker-compose.yml) prepared in this repo, which can be used with recent Docker version to
provide a backend. You just need to choose some profiles and specify them in a file [.env](docker/.env) using pattern `COMPOSE_PROFILES=profile1,profile2`:
* ```hazelcast``` runs Zeebe broker with Hazelcast exporter 
* ```kafka``` runs Zeebe broker with Kafka exporter 
* ```postgres``` runs PostgreSQL database
* ```mysql``` runs MySQL database

Then run ```docker compose up``` command.

With such a backend running, you can simply start debugging the ```ZeebeSimpleMonitorApp``` class in your IDE of choice.

### Reading 'text' fields in PostgreSQL

Some attributes in the entities are marked with ```@Lob``` annotation. This makes the JPA mapper use e.g. data
type ```text``` in PostgreSQL. This type needs some special treatment.

**Problem**: when you run SQL commands manually, e.g.
```select value_ from variable```
you will see just some 5-digit numbers.\
**Solution**: you need to convert the large object like this\
```SELECT convert_from(lo_get(value_::oid), 'UTF8') FROM variable```
