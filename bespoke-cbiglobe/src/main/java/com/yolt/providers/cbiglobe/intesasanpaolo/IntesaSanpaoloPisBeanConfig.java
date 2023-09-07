package com.yolt.providers.cbiglobe.intesasanpaolo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.CbiGlobeSepaPaymentProviderV3;
import com.yolt.providers.cbiglobe.common.config.CbiGlobeConfiguration;
import com.yolt.providers.cbiglobe.common.config.ProviderIdentification;
import com.yolt.providers.cbiglobe.common.pis.pec.initiate.CbiGlobeDefaultAccountToCurrencyMapper;
import com.yolt.providers.cbiglobe.common.pis.pec.initiate.CbiGlobeDefaultInstructedAmountToCurrencyMapper;
import com.yolt.providers.cbiglobe.common.pis.pec.initiate.CbiGlobeInitiatePaymentHttpRequestBodyProvider;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IntesaSanpaoloPisBeanConfig {

    @Bean("IntesaSanpaoloSepaPaymentProviderV3")
    public CbiGlobeSepaPaymentProviderV3 intesaSanpaoloSepaPaymentProviderV3(@Qualifier("CbiGlobe") final ObjectMapper objectMapper,
                                                                             final MeterRegistry meterRegistry,
                                                                             final IntesaSanpaoloProperties properties) {
        var intesaSanpaoloInitiatePaymentHttpRequestBodyProvider = new CbiGlobeInitiatePaymentHttpRequestBodyProvider(
                new CbiGlobeDefaultAccountToCurrencyMapper(),
                new CbiGlobeDefaultInstructedAmountToCurrencyMapper());
        return CbiGlobeConfiguration.createCbiGlobeSepaPaymentProviderV3(
                intesaSanpaoloInitiatePaymentHttpRequestBodyProvider,
                objectMapper,
                meterRegistry,
                properties,
                new ProviderIdentification("INTESA_SANPAOLO", "Intesa Sanpaolo", ProviderVersion.VERSION_3),
                ConsentValidityRules.EMPTY_RULES_SET);
    }
}
