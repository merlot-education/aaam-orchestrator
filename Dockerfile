FROM maven:3-eclipse-temurin-17-alpine AS build
COPY . /opt/
RUN mvn -ntp -f /opt/pom.xml clean package

FROM eclipse-temurin:17-jre-alpine
COPY --from=build /opt/target/aaam-orchestrator-*.jar /opt/aaam-orchestrator.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/aaam-orchestrator.jar"]
