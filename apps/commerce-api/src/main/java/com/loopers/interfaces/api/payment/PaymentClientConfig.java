package com.loopers.interfaces.api.payment;

import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentClientConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Request.Options options() {
        return new Request.Options(
            5000,  // connectTimeout (5초)
            10000, // readTimeout (10초)
            true   // followRedirects
        );
    }
}
