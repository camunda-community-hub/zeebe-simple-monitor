FROM docker.consulting.camunda.com/jdk8

ADD target/zeebe-simple-monitor.jar /
CMD ["java","-jar","/zeebe-simple-monitor.jar"]

EXPOSE 8080