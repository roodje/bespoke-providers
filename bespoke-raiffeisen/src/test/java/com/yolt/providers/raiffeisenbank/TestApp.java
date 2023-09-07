package com.yolt.providers.raiffeisenbank;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "com.yolt.providers.raiffeisenbank")
@Import(TestConfiguration.class)
public class TestApp {
}