package com.yolt.providers.unicredit.hypovereinsbank;

import com.yolt.providers.unicredit.common.config.UniCreditBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.unicredit.hypovereinsbank")
public class HypoVereinsbankProperties extends UniCreditBaseProperties {
}
