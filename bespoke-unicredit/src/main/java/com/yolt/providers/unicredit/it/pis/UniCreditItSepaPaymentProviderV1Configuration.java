package com.yolt.providers.unicredit.it.pis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.unicredit.common.data.mapper.UniCreditAuthMeansMapperV1;
import com.yolt.providers.unicredit.common.data.mapper.UniCreditSepaPaymentTransactionStatusMapperV1;
import com.yolt.providers.unicredit.common.pis.UniCreditSepaPaymentProvider;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpClientFactory;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpHeadersProducerV1;
import com.yolt.providers.unicredit.common.service.UniCreditAuthenticationMeansProducerV1;
import com.yolt.providers.unicredit.common.service.UniCreditSepaPaymentServiceV1;
import com.yolt.providers.unicredit.common.util.ProviderInfo;
import com.yolt.providers.unicredit.it.UniCreditItProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;

@Configuration
public class UniCreditItSepaPaymentProviderV1Configuration {

    @Bean("UniCreditItSepaPaymentProviderV1")
    public UniCreditSepaPaymentProvider getUniCreditItSepaPaymentProvider(final MeterRegistry meterRegistry,
                                                                          final @Qualifier("Unicredit") ObjectMapper objectMapper,
                                                                          final UniCreditItProperties properties) {
        return new UniCreditSepaPaymentProvider(
                new UniCreditAuthenticationMeansProducerV1(),
                new UniCreditSepaPaymentServiceV1(new UniCreditHttpClientFactory(meterRegistry, objectMapper, new UniCreditHttpHeadersProducerV1()),
                        new UniCreditAuthMeansMapperV1(),
                        properties,
                        new UniCreditSepaPaymentTransactionStatusMapperV1()),
                new ProviderInfo("UNICREDIT", "UniCredit",
                        ProviderVersion.VERSION_1),
                new ConsentValidityRules(new HashSet<>(Arrays.asList(
                        "Login PSD2 - Completa operazione richiesta da TPP",
                        "Codice Adesione",
                        "Pin",
                        "ACCEDI"
                )))
        );
    }
}
