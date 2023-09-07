package com.yolt.providers.sparkassenandlandesbanks.lbbw;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.sparkassenandlandesbanks.common.LandesbanksDataProvider;
import com.yolt.providers.sparkassenandlandesbanks.common.mapper.SparkassenAndLandesbanksAccountMapper;
import com.yolt.providers.sparkassenandlandesbanks.common.rest.SparkassenAndLandesbanksRestTemplateService;
import com.yolt.providers.sparkassenandlandesbanks.common.service.SparkassenAndLandesbanksAuthenticationService;
import com.yolt.providers.sparkassenandlandesbanks.common.service.SparkassenAndLandesbanksFetchDataService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.Arrays;
import java.util.HashSet;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;

@Configuration
public class LbbwBeanConfig {

    @Bean("LbbwDataProviderV1")
    public LandesbanksDataProvider lbbwDataProviderV1(MeterRegistry registry,
                                                      @Qualifier("SparkassenAndLandesbanksObjectMapper") ObjectMapper objectMapper,
                                                      LbbwProperties lbbwProperties,
                                                      Clock clock) {
        SparkassenAndLandesbanksRestTemplateService lbbwRestTemplateService = new SparkassenAndLandesbanksRestTemplateService(registry, objectMapper, lbbwProperties, clock);
        SparkassenAndLandesbanksAccountMapper accountMapper = new SparkassenAndLandesbanksAccountMapper(clock);
        return new LandesbanksDataProvider(
                new SparkassenAndLandesbanksAuthenticationService(lbbwRestTemplateService),
                objectMapper,
                new SparkassenAndLandesbanksFetchDataService(lbbwRestTemplateService, accountMapper),
                lbbwProperties.getBankCode(),
                "LBBW",
                "Landesbank Baden-Württemberg",
                VERSION_1,
                new ConsentValidityRules(new HashSet<>(Arrays.asList("PIN", "Um den Zugriff zu erteilen, melden Sie sich bitte zun"))),
                clock
        );
    }
}
