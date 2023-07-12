# CKO Bank Service

The CKO Bank Service is a Java-based application designed to act as an acquiring bank and accept card payments. It
provides functionalities to tokenize cards, make card payments and keep track for transactions.

## Features

- Tokenise and test card details
- Perform card entry validations
- Route cards to the appropriate card processor e.g. MasterCard, Visa, etc.
- Tracks transaction per beneficiary account
- Supports callbacks

## Technologies Used

- Java
- Spring Framework
- Spring Boot
- Spring Data JPA
- JUnit 5 (for unit testing)
- Mockito (for mocking dependencies in tests)
- H2 in-memory database

## Prerequisites

- Java JDK (version 17)

## Running the Application

Open a terminal and navigate to the root directory of the project.

Run the following command to start the application:

- `mvn clean install spring-boot:run` with maven
- `./mvnw clean install spring-boot:run` without maven

This command will start the CKO Bank application, and start up the H2 database with test cards loaded.

Once the application is up and running, you can access the API endpoints at http://localhost:3000.

You can also view the API documentation via swagger at http://localhost:3000/swagger-ui/index.html

If this port is not available, it can be changed in the
file [application.properties](src%2Fmain%2Fresources%2Fapplication.properties)

### Guides

The following are test cards available:

#### Successful Card Details

```
Card Number: 5555555555554444
Name On Card: John Doe
CVV: 111
Expiry Month: 10
Expiry Year: (Current year + 5 years in YYYY format)
```

#### Expired Card Details

```
Card Number: 4111111111111111
Name On Card: Jane Doe
CVV: 222
Expiry Month: 11
Expiry Year: (Current year + 5 years in YYYY format)
```

#### Blocked Card Details

```
Card Number: 378282246310005
Name On Card: Peter Parker
CVV: 333
Expiry Month: 12
Expiry Year: (Current year - 5 years in YYYY format)
```
