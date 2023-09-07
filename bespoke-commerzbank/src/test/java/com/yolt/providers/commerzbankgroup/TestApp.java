package com.yolt.providers.commerzbankgroup;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "com.yolt.providers.commerzbankgroup")
@Import(TestConfiguration.class)
public class TestApp {
}
