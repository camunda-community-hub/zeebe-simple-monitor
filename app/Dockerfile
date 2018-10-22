FROM openjdk:8-jre-alpine

EXPOSE 8080

COPY target/*.jar /usr/local/zeebe-simple-monitor.jar

ENTRYPOINT ["java", "-jar", "/usr/local/zeebe-simple-monitor.jar"]
