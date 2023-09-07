package com.yolt.providers.stet.bnpparibasgroup.hellobank.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component("HelloBankStetProperties")
//TODO name for bean should be removed in C4PO-9738
@ConfigurationProperties("lovebird.stet.bnpparibasgroup.hellobank")
public class HelloBankProperties extends DefaultProperties {
}
