package com.yolt.providers.kbcgroup.kbcbank.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.kbcgroup.common.mapper.KbcGroupAccountMapper;
import com.yolt.providers.kbcgroup.common.rest.KbcGroupRestTemplateService;
import com.yolt.providers.kbcgroup.common.service.KbcGroupAuthenticationService;
import com.yolt.providers.kbcgroup.common.service.KbcGroupFetchDataService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class KbcBankBeanConfig {

    @Bean("KbcBankRestTemplateService")
    public KbcGroupRestTemplateService getRestTemplateService(@Qualifier("KbcGroupObjectMapper") ObjectMapper objectMapper,
                                                              KbcBankProperties properties,
                                                              Clock clock) {
        return new KbcGroupRestTemplateService(objectMapper, properties, clock);
    }

    @Bean("KbcBankAuthenticationService")
    public KbcGroupAuthenticationService getAuthenticationService(KbcBankProperties properties,
                                                                  @Qualifier("KbcBankRestTemplateService") final KbcGroupRestTemplateService restTemplateService) {
        return new KbcGroupAuthenticationService(restTemplateService, properties);
    }

    @Bean("KbcBankFetchDataService")
    public KbcGroupFetchDataService getFetchDataService(KbcBankProperties properties,
                                                        @Qualifier("KbcBankRestTemplateService") final KbcGroupRestTemplateService restTemplateService,
                                                        Clock clock) {
        return new KbcGroupFetchDataService(restTemplateService, new KbcGroupAccountMapper(clock), properties);
    }
}
