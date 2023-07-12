package com.checkout.paymentgateway.integration;

import com.checkout.paymentgateway.api.ApiError;
import com.checkout.paymentgateway.integration.exception.CkoClientException;
import com.checkout.paymentgateway.integration.request.CreatePaymentRequest;
import com.checkout.paymentgateway.integration.request.TokeniseCardRequest;
import com.checkout.paymentgateway.integration.response.GetPaymentResponse;
import com.checkout.paymentgateway.integration.response.GetPaymentsResponse;
import com.checkout.paymentgateway.integration.response.TokeniseCardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class CkoClient {

    static final String PATH_PAYMENT = "/api/payments";

    static final String PATH_ACCOUNT_PAYMENT = "/api/payments/account";

    static final String PATH_TOKENISE_CARD = "/api/cards/tokenise-card";

    private final WebClient webClient;


    public TokeniseCardResponse tokeniseCard(TokeniseCardRequest request) {
        try {
            Mono<TokeniseCardResponse> response = webClient.post()
                    .uri(PATH_TOKENISE_CARD)
                    .body(BodyInserters.fromValue(request))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        // Extract error body as specific type
                        return clientResponse.bodyToMono(ApiError.class)
                                .flatMap(errorResponse -> Mono.error(new CkoClientException(errorResponse.getMessage()))
                                );
                    })
                    .bodyToMono(TokeniseCardResponse.class)
                    .retryWhen(Retry.backoff(1, Duration.ofSeconds(1))
                            .filter(throwable -> throwable instanceof CkoClientException)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure())
                    );

            return response.block();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }

    }

    public Mono<GetPaymentResponse> submitPayment(CreatePaymentRequest request, String idempotencyKey) {
        try {
            return webClient.post()
                    .uri(PATH_PAYMENT)
                    .header("X-Idempotency-Key", idempotencyKey)
                    .body(BodyInserters.fromValue(request))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        // Extract error body as specific type
                        return clientResponse.bodyToMono(ApiError.class)
                                .flatMap(errorResponse -> Mono.error(new CkoClientException(errorResponse.getMessage()))
                                );
                    })
                    .bodyToMono(GetPaymentResponse.class)
                    .retryWhen(Retry.backoff(1, Duration.ofSeconds(2))
                            .filter(throwable -> throwable instanceof CkoClientException)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure())
                    );
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }

    }

    public GetPaymentResponse getPayment(Long paymentId) {
        String url = String.format("%s/%s", PATH_PAYMENT, paymentId);
        try {
            Mono<GetPaymentResponse> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        // Extract error body as specific type
                        return clientResponse.bodyToMono(ApiError.class)
                                .flatMap(errorResponse -> Mono.error(new CkoClientException(errorResponse.getMessage()))
                                );
                    })
                    .bodyToMono(GetPaymentResponse.class)
                    .retryWhen(Retry.backoff(1, Duration.ofSeconds(2))
                            .filter(throwable -> throwable instanceof CkoClientException)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure())
                    );

            return response.block();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }

    }

    public GetPaymentsResponse getAccountPayments(String account) {
        String url = String.format("%s/%s", PATH_ACCOUNT_PAYMENT, account);
        try {
            Mono<GetPaymentsResponse> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        // Extract error body as specific type
                        return clientResponse.bodyToMono(ApiError.class)
                                .flatMap(errorResponse -> Mono.error(new CkoClientException(errorResponse.getMessage()))
                                );
                    })
                    .bodyToMono(GetPaymentsResponse.class)
                    .retryWhen(Retry.backoff(1, Duration.ofSeconds(2))
                            .filter(throwable -> throwable instanceof CkoClientException)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure())
                    );

            return response.block();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }

    }
}
