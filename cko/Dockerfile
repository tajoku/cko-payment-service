FROM maven:3.8.4-openjdk-17-slim AS BUILD

COPY . /opt/app
WORKDIR /opt/app
RUN mvn install

FROM openjdk:17-slim
ENV JAR='cko-0.0.1-SNAPSHOT.jar'
COPY --from=BUILD /opt/app/target/classes /opt/app/classes
COPY --from=BUILD /opt/app/target/${JAR} /opt/app/${JAR}
WORKDIR /opt/app

# Add docker-compose-wait tool -------------------
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.7.2/wait ./wait
RUN chmod +x ./wait

ENTRYPOINT ["/bin/sh", "-c", "./wait && java -jar ${JAR}"]
