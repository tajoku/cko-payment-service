package com.checkout.cko.service;

import com.checkout.cko.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackService {

    private final WebClient webClient;


    public void sendCallback(String callbackUrl, PaymentStatus paymentStatus, String description) {
        try {
            // Build the URL with parameters
            String url = UriComponentsBuilder.fromUriString(callbackUrl)
                    .queryParam("status", paymentStatus.name())
                    .queryParam("description", paymentStatus.name())
                    .toUriString();

            webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe();

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
