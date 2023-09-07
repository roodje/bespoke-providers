package com.yolt.providers.monorepogroup.chebancagroup;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "com.yolt.providers.monorepogroup.chebancagroup")
@Import(CheBancaGroupTestConfiguration.class)
public class CheBancaGroupTestApp {
}
