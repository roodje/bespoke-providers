package com.yolt.providers.cbiglobe.posteitaliane;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.CbiGlobeSepaPaymentProviderV3;
import com.yolt.providers.cbiglobe.common.config.CbiGlobeConfiguration;
import com.yolt.providers.cbiglobe.common.config.ProviderIdentification;
import com.yolt.providers.cbiglobe.posteitaliane.pis.pec.initiate.PosteItalianeInitiatePaymentHttpRequestBodyProvider;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;

@Configuration
public class PosteItalianePisBeanConfig {

    @Bean("PosteItalianeSepaPaymentProviderV3")
    public CbiGlobeSepaPaymentProviderV3 posteItalianeSepaPaymentProviderV3(@Qualifier("CbiGlobe") final ObjectMapper objectMapper,
                                                                            final MeterRegistry meterRegistry,
                                                                            final PosteItalianeProperties properties) {
        var posteItalianeInitiatePaymentHttpRequestBodyProvider = new PosteItalianeInitiatePaymentHttpRequestBodyProvider();
        return CbiGlobeConfiguration.createCbiGlobeSepaPaymentProviderV3(
                posteItalianeInitiatePaymentHttpRequestBodyProvider,
                objectMapper,
                meterRegistry,
                properties,
                new ProviderIdentification("POSTE_ITALIANE", "Poste Italiane", ProviderVersion.VERSION_3),
                getConsentValidityRules());
    }

    public ConsentValidityRules getConsentValidityRules() {
        return new ConsentValidityRules(new HashSet<>(Arrays.asList(
                "Inserisci le tue",
                "credenziali",
                "NOME",
                "UTENTE",
                "PASSWORD",
                "accedi"
        )));
    }
}
