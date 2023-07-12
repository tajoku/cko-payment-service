package com.checkout.cko.configuration;

import com.checkout.cko.enums.CardStatus;
import com.checkout.cko.enums.CardType;
import com.checkout.cko.enums.Currency;
import com.checkout.cko.model.Card;
import com.checkout.cko.repository.CardRepository;
import com.checkout.cko.service.processor.CardProcessor;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {

    private static final int DEFAULT_TIMEOUT = 5000;

    private final CardRepository cardRepository;

    private final List<CardProcessor> cardProcessorList;

    @Override
    public void run(String... args) throws Exception {
        cardRepository.saveAll(Arrays.asList(getSuccessfulCard(), getExpiredCard(), getBlockedCard()));
    }


    @Bean
    public Map<CardType, CardProcessor> cardProcessors() {
        return cardProcessorList.stream()
                .collect(Collectors.toMap(CardProcessor::getCardType, cardProcessor -> cardProcessor));
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(DEFAULT_TIMEOUT);
        return new RestTemplate(factory);
    }

    @Bean
    public WebClient webClient() {
        HttpClient client = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_TIMEOUT)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS));
                });

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(client))
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private Card getSuccessfulCard() {
        return Card.builder()
                .cardNumber("5555555555554444")
                .nameOnCard("John Doe")
                .cvv("111")
                .cardStatus(CardStatus.ACTIVE)
                .token(UUID.randomUUID().toString())
                .currency(Currency.GBP)
                .cardType(CardType.MASTERCARD)
                .expiryMonth(10)
                .expiryYear(LocalDate.now().getYear() + 5)
                .build();
    }

    private Card getExpiredCard() {
        return Card.builder()
                .cardNumber("4111111111111111")
                .nameOnCard("Jane Doe")
                .cvv("222")
                .cardStatus(CardStatus.ACTIVE)
                .token(UUID.randomUUID().toString())
                .currency(Currency.GBP)
                .cardType(CardType.VISA)
                .expiryMonth(11)
                .expiryYear(LocalDate.now().getYear() - 5)
                .build();
    }

    private Card getBlockedCard() {
        return Card.builder()
                .cardNumber("378282246310005")
                .nameOnCard("Peter Parker")
                .cvv("333")
                .cardStatus(CardStatus.BLOCKED)
                .token(UUID.randomUUID().toString())
                .currency(Currency.GBP)
                .cardType(CardType.AMERICAN_EXPRESS)
                .expiryMonth(12)
                .expiryYear(LocalDate.now().getYear() + 5)
                .build();
    }
}
