package com.yolt.providers.amexgroup;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.Clock;

@SpringBootApplication
@Import(TestConfiguration.class)
public class TestApp {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
