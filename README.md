# MERLOT AAAM Orchestrator
The Authentication-Authorization-Account-Manager (AAAM) Orchestrator is a microservice in the MERLOT marketplace
which handles fetching account information from the authentication backend (e.g. Keycloak).

This currently includes information such as the enrolled users in a particular organisation.

## Development

To start development for the MERLOT marketplace, please refer to [this document](https://github.com/merlot-education/.github/blob/main/Docs/DevEnv.md)
to set up a local WSL development environment of all relevant services.
This is by far the easiest way to get everything up and running locally.

## Structure

```
├── src/main/java/eu/merloteducation/aaamorchestrator
│   ├── config          # configuration-related components
│   ├── controller      # external REST API controllers
│   ├── models          # internal data models of user-related data
│   ├── security        # configuration for route-based authentication
│   ├── service         # internal services for processing data from the controller layer
│   ├── views           # JSON views for DTO serialization
```

## Dependencies
- A properly set-up keycloak instance (quay.io/keycloak/keycloak:20.0.5)

## Build

To build this microservice you need to provide a GitHub read-only token in order to be able to fetch maven packages from
GitHub. You can create this token at https://github.com/settings/tokens with at least the scope "read:packages".
Then set up your ~/.m2/settings.xml file as follows:

    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

        <servers>
            <server>
                <id>github</id>
                <username>REPLACEME_GITHUB_USER</username>
                <!-- Public token with `read:packages` scope -->
                <password>REPLACEME_GITHUB_TOKEN</password>
            </server>
        </servers>
    </settings>

Afterward you can build the service with

    mvn clean package

## Run

    java -jar target/aaam-orchestrator-X.Y.Z.jar

Replace the X.Y.Z with the respective version of the service.

## Deploy (Docker)

This microservice can be deployed as part of the full MERLOT docker stack at
[localdeployment](https://github.com/merlot-education/localdeployment).

## Deploy (Helm)
TODO