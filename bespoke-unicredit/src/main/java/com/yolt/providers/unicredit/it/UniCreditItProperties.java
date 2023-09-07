package com.yolt.providers.unicredit.it;


import com.yolt.providers.unicredit.common.config.UniCreditBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.unicredit.it")
public class UniCreditItProperties extends UniCreditBaseProperties {
}
