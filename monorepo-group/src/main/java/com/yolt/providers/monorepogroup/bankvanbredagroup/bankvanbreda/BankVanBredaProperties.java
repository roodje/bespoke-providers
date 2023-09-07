package com.yolt.providers.monorepogroup.bankvanbredagroup.bankvanbreda;

import com.yolt.providers.monorepogroup.bankvanbredagroup.common.config.BankVanBredaGroupProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.bankvanbredagroup.bankvanbreda")
public class BankVanBredaProperties extends BankVanBredaGroupProperties {

}