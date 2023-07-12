# CKO Payment Service

This service consists of two modules: **[CKO](cko)** and **[Payment Gateway](payment-gateway)**. The **[CKO](cko)**
module acts as an acquiring
bank, handling and processing card transactions and also validating card details.
The **[Payment Gateway](payment-gateway)** module serves as a payment gateway, facilitating communication between
merchants and the **[CKO](cko)**
module.

## CKO Module

The **[CKO](cko)** module is responsible for handling card transactions and validating card details.
It acts as an acquiring bank and provides APIs for processing payments.

#### Test Cards

The **[CKO](cko)** module supports the following test cards for different scenarios:

**Successful Card**: This card can be used to simulate a successful payment transaction.

**Blocked Card**: This card can be used to simulate a scenario where the card is blocked and cannot be used for
payments.

**Expired Card**: This card can be used to simulate a scenario where the card has expired and cannot be used for
payments.

Make sure to use the appropriate test card based on the desired test case scenario.
Test card values can be found in the **[CKO](cko)** [README.md](cko%2FREADME.md)

## Payment Gateway Module

The **[Payment Gateway](payment-gateway)** module acts as a gateway between merchants and the **[CKO](cko)** module.
It handles merchant requests and communicates with the **[CKO](cko)** module to process payments.
Test merchant values can be found in the **[Payment Gateway](payment-gateway)** [README.md](payment-gateway%2FREADME.md)

#### Endpoints

The **[Payment Gateway](payment-gateway)** module provides the following endpoints:

`POST /api/{merchantId}/pay/new-card`: This endpoint is used to initiate a new payment transaction with new card
details.

`POST /api/{merchantId}/pay/charge-card`: This endpoint is used to initiate a new payment transaction with an existing
card.

`GET /api/{merchantId}/payments`: This endpoint is used to retrieve all the payments initiated by a specific merchant.

`GET /api/{merchantId}/payments/{paymentId}`: This endpoint is used to retrieve the details of a payment transaction
including the card details used, by providing the payment ID.

`GET /api/{merchantId}/{paymentReference}/callback`: This endpoint is used to accept callback notifications of the
payment
status from **[CKO](cko)**.

Merchants can send requests to these endpoints to perform payment transactions and retrieve payment status.

## Prerequisites

Before running the service, ensure that you have the following prerequisites installed:

- Java Development Kit (JDK) 17
- Apache Maven
- Docker (Optional)

## Getting Started

To run the service locally, follow these steps:

Clone the repository: `git clone https://github.com/tajoku/cko-payment-service.git`

### Manually running each service with Maven

Navigate to the **[CKO](cko)** project root directory: `cd ./cko `

Build the project using Maven: `mvn clean install`

Start the **[CKO](cko)** module by running: `mvn spring-boot:run` or `./mvnw spring-boot:run` if maven is not installed

Once **[CKO](cko)** is up and running, navigate to the **[Payment Gateway](payment-gateway)** project root
directory: `cd ./payment-gateway`

Start the **[Payment Gateway](payment-gateway)** module by running: `mvn spring-boot:run` or `./mvnw spring-boot:run` if
maven is not installed
**Note**: The **[Payment Gateway](payment-gateway)** runs with the command `-Dspring.profiles.active=dev` when running
in Docker. This is due
to the application mapping in the docker compose file.

### Running with Docker

To simplify the setup process, the project provides a [Docker Compose](docker-compose.yaml) file. Follow the steps below
to run the applications using Docker:

Make sure Docker is installed and running on your machine.

Open a terminal and navigate to the root directory of the project.

Run the following command to start the applications:

```
make docker-up
```

This command will start both services with **[CKO](cko)** being the first since **[Payment Gateway](payment-gateway)**
relies on it for bank
communications.

The services will now be running locally, and you can access the endpoints of the **[Payment Gateway](payment-gateway)**
module to perform
payment transactions at http://localhost:8080.
The API swagger documentation can be found here http://localhost:8080/swagger-ui/index.html

## Configuration

Both modules have their configuration files located in the src/main/resources directory. You can customize the
configurations according to your requirements, such as database connection settings, API endpoint URLs, and logging
configurations.
Any port changes will also need to be updated in the [Docker Compose](docker-compose.yaml) file if you wish to run the
application on docker.

## Testing

To run the tests for each module, navigate to the respective module directory (**[CKO](cko)** or payment-gateway) and
run `mvn test` or `./mvnw test`. This will execute the unit tests for the module.

## API Documentation

The API documentation for the **[Payment Gateway](payment-gateway)** module can be accessed
at http://localhost:8080/swagger-ui.html once the
service is running. It provides detailed information about the available endpoints, request/response formats, and
possible error responses.

## Key Features

- Retry and backoff mechanism for requests to **[CKO](cko)** from the **[Payment Gateway](payment-gateway)**
- **[CKO](cko)** supports idempotency for up to 1 minute to prevent multiple payment submissions
- Supports Mocked Fraud service for checking fraudulent activities
- Supports Mocked Notification service for merchant webhooks. This will notify merchants when a payment has been updated
- **[CKO](cko)** supports card tokenization
- Supports payment through card details as well as through card tokens
- Upholds [PCI DSS](https://www.pcisecuritystandards.org/) compliance by only storing full card details in **[CKO](cko)
  ** and
  persisting a tokenized version of the card details in the **[Payment Gateway](payment-gateway)**.
- Asynchronous payment submissions for seamless user experience
- Includes unit and integrations tests

## Future Considerations

- Include better and more secure communications via mTLS, client secrets or JWT.
- Convert webhooks to POST with an encrypted webhook signature included to
  prevent [Man-in-the-middle attacks](https://en.wikipedia.org/wiki/Man-in-the-middle_attack).
- Implement actual Notification and Fraud services
- Implement actual card processors
- Introduce other payment methods e.g. direct debit, bank transfer, etc.
- Introduce message brokers such as Kafka for a more event driven architecture between services (e.g. Fraud and
  Notification)
- Redact any sensitive data or payer information in the logs