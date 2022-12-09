FROM azul/zulu-openjdk-alpine:17-jre

RUN apk update && \
    apk upgrade && \
    apk add --no-cache libc6-compat libstdc++ && \
    rm -rf /var/cache/apk/*

COPY target/zeebe-simple-monitor-2.4.2.jar /app/simple-monitor.jar
COPY src/main/resources/application.yaml /config/

EXPOSE 8082

ENTRYPOINT [ "java","-XX:+UnlockExperimentalVMOptions", \
"-XX:+ExitOnOutOfMemoryError", \
"-XX:MaxRAMPercentage=50", \
"-XX:+UseParallelGC", \
"-XX:MinHeapFreeRatio=5", \
"-XX:MaxHeapFreeRatio=10", \
"-XX:GCTimeRatio=4", \
"-XX:AdaptiveSizePolicyWeight=90", \
"-jar",\
"/app/simple-monitor.jar" ]