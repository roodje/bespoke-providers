package com.yolt.providers.volksbank.asn.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.volksbank.common.config.ProviderIdentification;
import com.yolt.providers.volksbank.common.config.VolksbankBeanConfigV2;
import com.yolt.providers.volksbank.common.pis.VolksbankSepaPaymentProviderV3;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ASNPisBeanConfig {

    private static final String IDENTIFIER = "ASN_BANK";
    private static final String DISPLAY_NAME = "ASN Bank";

    @Bean("ASNBankSepaPaymentProviderV3")
    public VolksbankSepaPaymentProviderV3 asnSepaPaymentProviderV3(@Qualifier("Volksbank") final ObjectMapper objectMapper,
                                                                   final MeterRegistry meterRegistry,
                                                                   final ASNProperties properties) {
        return VolksbankBeanConfigV2.createVolksbankSepaPaymentProviderV3(objectMapper,
                meterRegistry,
                properties,
                new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, ProviderVersion.VERSION_3),
                ConsentValidityRules.EMPTY_RULES_SET);
    }
}
