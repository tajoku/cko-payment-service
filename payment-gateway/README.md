# Payment Gateway Service

The Payment Gateway Service is a Java-based application designed to act as a gateway for accepting payments to the CKO.
It provides functionalities to tokenize cards, make card payments and keep track for transactions.

## Features

- Tokenise and test card details
- Perform card entry validations
- Notify merchant of any payment status changes
- Fraud checks
- Tracks transaction per merchant
- Direct integration to CKO Bank Service

## Technologies Used

- Java
- Spring Framework
- Spring Boot
- Spring Data JPA
- JUnit 5 (for unit testing)
- Mockito (for mocking dependencies in tests)
- MockWebServer (for mocking integration tests)
- H2 in-memory database

## Prerequisites

- Java JDK (version 17)

## Running the Application

Open a terminal and navigate to the root directory of the project.

Run the following command to start the application:

- `mvn clean install spring-boot:run` with maven
- `./mvnw clean install spring-boot:run` without maven

This command will start the Payment Gateway application, and start up the H2 database with a test merchant loaded.

Once the application is up and running, you can access the API endpoints at http://localhost:8080.

You can also view the API documentation via swagger at http://localhost:8080/swagger-ui/index.html

If this port is not available, it can be changed in the
file [application.properties](src%2Fmain%2Fresources%2Fapplication.properties)

### Guides

The following test merchant is available:

```
ID: 1
Name: Example Merchant
Merchant type: CLOTHING
Bank name: CKO Bank
Bank account: 2000000
```

