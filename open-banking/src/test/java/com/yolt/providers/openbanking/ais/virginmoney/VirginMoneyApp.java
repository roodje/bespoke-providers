package com.yolt.providers.openbanking.ais.virginmoney;

import com.yolt.providers.openbanking.ais.TestConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(TestConfiguration.class)
public class VirginMoneyApp {
    // Limited app context
}
