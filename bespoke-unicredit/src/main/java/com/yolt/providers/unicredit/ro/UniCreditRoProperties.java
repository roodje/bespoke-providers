package com.yolt.providers.unicredit.ro;

import com.yolt.providers.unicredit.common.config.UniCreditBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.unicredit.ro")
public class UniCreditRoProperties extends UniCreditBaseProperties {
}
