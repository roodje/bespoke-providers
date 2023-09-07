package com.yolt.providers.axabanque;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AxaBanqueTestConfiguration {

    @Bean
    public Clock mockedClock() {
        return Clock.systemUTC();
    }

}
