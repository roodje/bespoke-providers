package com.yolt.providers.alpha.alphabankromania;

import com.yolt.providers.alpha.TestConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "com.yolt.providers.alpha.alphabankromania")
@Import(TestConfiguration.class)
public class AlphaBankRomaniaTestApp {
}
