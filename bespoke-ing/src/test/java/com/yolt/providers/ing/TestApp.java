package com.yolt.providers.ing;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "com.yolt.providers.ing")
@Import(TestConfiguration.class)
public class TestApp {
}