package com.yolt.providers.monorepogroup.bankvanbredagroup;

import com.yolt.providers.monorepogroup.TestConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "com.yolt.providers.monorepogroup.bankvanbredagroup")
@Import(TestConfiguration.class)
public class BankVanBredaTestApp {
}