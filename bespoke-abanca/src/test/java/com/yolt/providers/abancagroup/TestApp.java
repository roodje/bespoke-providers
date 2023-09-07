package com.yolt.providers.abancagroup;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "com.yolt.providers.abancagroup")
@Import(TestConfiguration.class)
public class TestApp {
}