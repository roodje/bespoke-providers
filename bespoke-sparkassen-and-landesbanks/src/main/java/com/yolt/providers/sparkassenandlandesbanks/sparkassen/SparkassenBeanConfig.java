package com.yolt.providers.sparkassenandlandesbanks.sparkassen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.sparkassenandlandesbanks.common.mapper.SparkassenAndLandesbanksAccountMapper;
import com.yolt.providers.sparkassenandlandesbanks.common.rest.SparkassenAndLandesbanksRestTemplateService;
import com.yolt.providers.sparkassenandlandesbanks.common.service.SparkassenAndLandesbanksAuthenticationService;
import com.yolt.providers.sparkassenandlandesbanks.common.service.SparkassenAndLandesbanksFetchDataService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class SparkassenBeanConfig {

    @Bean("SparkassenRestTemplateService")
    public SparkassenAndLandesbanksRestTemplateService getRestTemplateService(MeterRegistry registry,
                                                                              @Qualifier("SparkassenAndLandesbanksObjectMapper") ObjectMapper objectMapper,
                                                                              SparkassenProperties properties,
                                                                              Clock clock) {
        return new SparkassenAndLandesbanksRestTemplateService(registry, objectMapper, properties, clock);
    }

    @Bean("SparkassenAuthenticationService")
    public SparkassenAndLandesbanksAuthenticationService getAuthenticationService(@Qualifier("SparkassenRestTemplateService") SparkassenAndLandesbanksRestTemplateService restTemplateService) {
        return new SparkassenAndLandesbanksAuthenticationService(restTemplateService);
    }

    @Bean("SparkassenFetchDataService")
    public SparkassenAndLandesbanksFetchDataService getFetchDataService(@Qualifier("SparkassenRestTemplateService") final SparkassenAndLandesbanksRestTemplateService restTemplateService,
                                                                        Clock clock) {
        SparkassenAndLandesbanksAccountMapper accountMapper = new SparkassenAndLandesbanksAccountMapper(clock);
        return new SparkassenAndLandesbanksFetchDataService(restTemplateService, accountMapper);
    }
}
