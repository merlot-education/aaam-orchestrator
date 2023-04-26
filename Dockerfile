FROM maven:3.8-jdk-17-slim AS build
COPY . /opt/
RUN mvn -f /opt/pom.xml clean package

FROM eclipse-temurin:17-jre-alpine
COPY --from=build /opt/target/aaam-orchestrator-*.jar /opt/aaam-orchestrator.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/aaam-orchestrator.jar"]