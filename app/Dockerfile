FROM openjdk:8-jre-alpine

EXPOSE 8080

ARG JAR

COPY ${JAR} /usr/local/zeebe-simple-monitor.jar

ENTRYPOINT ["java", "-jar", "/usr/local/zeebe-simple-monitor.jar"]
