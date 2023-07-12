package com.checkout.cko;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CkoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CkoApplication.class, args);
    }

}
