package com.yolt.providers.openbanking.ais.hsbcgroup;

import com.yolt.providers.openbanking.ais.TestConfiguration;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({TestConfiguration.class, OpenbankingConfiguration.class})
public class HsbcGroupApp {
    // Limited app context
}
