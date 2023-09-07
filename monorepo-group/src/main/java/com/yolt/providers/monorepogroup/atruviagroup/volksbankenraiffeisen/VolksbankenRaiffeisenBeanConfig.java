package com.yolt.providers.monorepogroup.atruviagroup.volksbankenraiffeisen;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV3;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.atruviagroup.common.AtruviaGroupDataProvider;
import com.yolt.providers.monorepogroup.atruviagroup.common.authenticationmeans.DefaultAtruviaGroupAuthenticationMeansFactory;
import com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.*;
import com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps.*;
import com.yolt.providers.monorepogroup.atruviagroup.common.http.*;
import com.yolt.providers.monorepogroup.atruviagroup.common.mapper.AtruviaGroupAccountMapper;
import com.yolt.providers.monorepogroup.atruviagroup.common.mapper.AtruviaGroupDateConverter;
import com.yolt.providers.monorepogroup.atruviagroup.common.mapper.AtruviaGroupProviderStateMapper;
import com.yolt.providers.monorepogroup.atruviagroup.common.mapper.AtruviaGroupTransactionMapper;
import com.yolt.providers.monorepogroup.atruviagroup.common.service.fetchdata.AtruviaGroupFetchDataServiceV1;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.Clock;
import java.time.ZoneId;

import static com.yolt.providers.monorepogroup.atruviagroup.volksbankenraiffeisen.VolksbankenRaiffeisenSiteDetailsProvider.VOLKSBANKEN_RAIFFEISEN_DISPLAY_NAME;
import static com.yolt.providers.monorepogroup.atruviagroup.volksbankenraiffeisen.VolksbankenRaiffeisenSiteDetailsProvider.VOLKSBANKEN_RAIFFEISEN_PROVIDER_KEY;

@Configuration
class VolksbankenRaiffeisenBeanConfig {

    @Bean
    @Qualifier("VolksbankenRaiffeisen")
    public ObjectMapper getVolksbankenRaiffeisenMapper(Jackson2ObjectMapperBuilder mapperBuilder) {
        ObjectMapper mapper = mapperBuilder.build();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean("VolksbankenRaiffeisenProvider")
    public UrlDataProvider volksbankenRaiffeisenProvider(VolksbankenRaiffeisenProperties properties,
                                                         Clock clock,
                                                         MeterRegistry meterRegistry,
                                                         @Qualifier("VolksbankenRaiffeisen") ObjectMapper objectMapper) {
        var httpClientFactory = new AtruviaGroupHttpClientFactoryV1(objectMapper, meterRegistry,
                new AtruviaGroupHttpHeadersProducer(new AtruviaGroupSigningUtil(), objectMapper), new AtruviaCompositeHttpErrorHandler(new AtruviaGroupHttpErrorHandlerV2(), new DefaultHttpErrorHandlerV3()));
        var providerStateMapper = new AtruviaGroupProviderStateMapper(objectMapper);
        var dateConverter = new AtruviaGroupDateConverter(clock, ZoneId.of("Europe/Berlin"));
        var keyPairProvider = new KeyPairProvider();
        var bankSelectionStep = new InitialStateInspector(new ObtainRegionalBankSelectionStep(properties));
        var initialStep = new InputStateInspector(new ObtainUsernameAndPasswordStep(keyPairProvider));
        var scaMethodSelectionStep = new InputStateInspector(new CreateConsentAndEitherObtainChallengeMethodOrChallengeDataStep(keyPairProvider, clock));
        var obtainChallengeData = new InputStateInspector(new ObtainChallengeDataStep(keyPairProvider));
        var obtainChallengeOutcome = new InputStateInspector(new AuthorizeTheConsentAndFinishTheSCAStep());

        var stepsChain = bankSelectionStep
                .orElse(initialStep)
                .orElse(scaMethodSelectionStep)
                .orElse(obtainChallengeData)
                .orElse(obtainChallengeOutcome);

        var authenticationMeansFactory = new DefaultAtruviaGroupAuthenticationMeansFactory(VOLKSBANKEN_RAIFFEISEN_PROVIDER_KEY);
        var stepFactory = new StepFactory(clock, providerStateMapper, authenticationMeansFactory);
        return new AtruviaGroupDataProvider(VOLKSBANKEN_RAIFFEISEN_DISPLAY_NAME,
                VOLKSBANKEN_RAIFFEISEN_PROVIDER_KEY,
                ProviderVersion.VERSION_1,
                new AtruviaEmbeddedFlowProcess(properties, httpClientFactory, VOLKSBANKEN_RAIFFEISEN_PROVIDER_KEY, stepsChain),
                authenticationMeansFactory,
                stepFactory,
                new AtruviaGroupFetchDataServiceV1(
                        httpClientFactory,
                        new AtruviaGroupAccountMapper(dateConverter),
                        new AtruviaGroupTransactionMapper(dateConverter),
                        providerStateMapper,
                        properties,
                        dateConverter,
                        VOLKSBANKEN_RAIFFEISEN_DISPLAY_NAME));
    }
}
