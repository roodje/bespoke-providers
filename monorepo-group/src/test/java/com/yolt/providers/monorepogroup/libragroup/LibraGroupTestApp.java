package com.yolt.providers.monorepogroup.libragroup;

import com.yolt.providers.monorepogroup.TestConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "com.yolt.providers.monorepogroup.libragroup")
@Import(TestConfiguration.class)
public class LibraGroupTestApp {
}