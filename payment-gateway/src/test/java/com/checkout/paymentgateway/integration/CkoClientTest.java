package com.checkout.paymentgateway.integration;

import com.checkout.paymentgateway.integration.exception.CkoClientException;
import com.checkout.paymentgateway.integration.request.CreatePaymentRequest;
import com.checkout.paymentgateway.integration.request.TokeniseCardRequest;
import com.checkout.paymentgateway.integration.response.GetPaymentResponse;
import com.checkout.paymentgateway.integration.response.GetPaymentsResponse;
import com.checkout.paymentgateway.integration.response.TokeniseCardResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CkoClientTest {

    private CkoClient ckoClient;

    private static MockWebServer mockWebServer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void initialise() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        WebClient webClient = WebClient.create(String.format("http://localhost:%s",
                mockWebServer.getPort()));
        ckoClient = new CkoClient(webClient);

    }

    @Test
    void tokeniseCard_ShouldReturnTokeniseCardResponse() throws JsonProcessingException {
        // Mock the webClient response
        TokeniseCardRequest request = new TokeniseCardRequest();
        TokeniseCardResponse expectedResponse = new TokeniseCardResponse();

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expectedResponse))
                .addHeader("Content-Type", "application/json"));

        // Invoke the method
        TokeniseCardResponse response = ckoClient.tokeniseCard(request);


        assertEquals(expectedResponse, response);
    }


    @Test
    void tokeniseCard_ShouldHandleErrorStatus() throws JsonProcessingException {
        TokeniseCardRequest request = new TokeniseCardRequest();
        int times = 2;

        // Retry based on the number of retries in the client
        while (times > 0) {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
                    .setBody("{\"message\": \"Error message\"}")
                    .addHeader("Content-Type", "application/json"));
            times--;
        }

        assertThrows(CkoClientException.class, () -> ckoClient.tokeniseCard(request));
    }


    @Test
    void submitPayment_ShouldReturnGetPaymentResponse() throws JsonProcessingException {
        CreatePaymentRequest request = new CreatePaymentRequest();
        String idempotencyKey = "idempotencyKey";
        GetPaymentResponse expectedResponse = new GetPaymentResponse();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(objectMapper.writeValueAsString(expectedResponse))
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Idempotency-Key", idempotencyKey));

        StepVerifier
                .create(ckoClient.submitPayment(request, idempotencyKey))
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();
    }

    @Test
    void getPayment_ShouldReturnGetPaymentResponse() throws JsonProcessingException {
        Long paymentId = 1L;
        GetPaymentResponse expectedResponse = new GetPaymentResponse();

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expectedResponse))
                .addHeader("Content-Type", "application/json"));

        GetPaymentResponse response = ckoClient.getPayment(paymentId);

        assertEquals(expectedResponse, response);
    }

    @Test
    void getAccountPayments_ShouldReturnGetPaymentsResponse() throws JsonProcessingException {
        String account = "account";
        GetPaymentsResponse expectedResponse = new GetPaymentsResponse();


        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expectedResponse))
                .addHeader("Content-Type", "application/json"));

        GetPaymentsResponse response = ckoClient.getAccountPayments(account);

        assertEquals(expectedResponse, response);
    }

    @Test
    void submitPayment_WithErrorStatus_ShouldThrowCkoClientException() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        String idempotencyKey = "idempotencyKey";

        int times = 2;

        // Retry based on the number of retries in the client
        while (times > 0) {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
                    .setBody("{\"message\": \"Error message\"}")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Idempotency-Key", idempotencyKey));
            times--;
        }

        StepVerifier.create(ckoClient.submitPayment(request, idempotencyKey))
                .expectError(CkoClientException.class)
                .verify();
    }
}