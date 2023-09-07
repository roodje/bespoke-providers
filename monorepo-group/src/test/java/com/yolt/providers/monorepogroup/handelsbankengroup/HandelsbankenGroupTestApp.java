package com.yolt.providers.monorepogroup.handelsbankengroup;

import com.yolt.providers.monorepogroup.TestConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(TestConfiguration.class)
public class HandelsbankenGroupTestApp {
}
