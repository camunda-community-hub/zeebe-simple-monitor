FROM maven:latest as downloader

ENV HAZELCAST_EXPORTER_VERSION=0.7.0

RUN mvn dependency:get -B \
    -DgroupId="io.zeebe.hazelcast" -DartifactId="zeebe-hazelcast-exporter" \
    -Dversion="${HAZELCAST_EXPORTER_VERSION}" -Dpackaging="jar" -Dclassifier="jar-with-dependencies" -Dtransitive=false

RUN mvn dependency:copy -B \
    -Dartifact="io.zeebe.hazelcast:zeebe-hazelcast-exporter:${HAZELCAST_EXPORTER_VERSION}:jar" \
    -Dclassifier="jar-with-dependencies" \
    -DoutputDirectory=/tmp/ \
    -Dmdep.stripVersion=true


FROM camunda/zeebe:latest

COPY --from=downloader /tmp/zeebe-hazelcast-exporter-jar-with-dependencies.jar /usr/local/zeebe/lib/zeebe-hazelcast-exporter-jar-with-dependencies.jar
