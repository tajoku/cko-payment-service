package com.checkout.paymentgateway.configuration;

import com.checkout.paymentgateway.enums.MerchantType;
import com.checkout.paymentgateway.model.Merchant;
import com.checkout.paymentgateway.repository.MerchantRepository;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {

    @Value("${cko.host.url}")
    private String ckoUrl;

    private static final int DEFAULT_TIMEOUT = 5000;

    private final MerchantRepository merchantRepository;

    @Override
    public void run(String... args) throws Exception {
        merchantRepository.save(Merchant.builder()
                .bankAccountNumber("2000000")
                .name("Example Merchant")
                .bankName("CKO Bank")
                .merchantType(MerchantType.CLOTHING)
                .build());
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
                .baseUrl(ckoUrl)
                .clientConnector(new ReactorClientHttpConnector(client))
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}
