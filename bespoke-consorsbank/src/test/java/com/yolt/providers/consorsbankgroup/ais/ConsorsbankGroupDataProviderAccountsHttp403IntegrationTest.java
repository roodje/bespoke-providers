package com.yolt.providers.consorsbankgroup.ais;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.consorsbankgroup.ConsorsbankGroupSampleAuthMeans;
import com.yolt.providers.consorsbankgroup.ConsorsbankGroupTestApp;
import com.yolt.providers.consorsbankgroup.FakeRestTemplateManager;
import com.yolt.providers.consorsbankgroup.common.ais.ConsorsbankGroupDataProvider;
import com.yolt.providers.consorsbankgroup.common.ais.DefaultAccessMeans;
import lombok.SneakyThrows;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@ActiveProfiles("consorsbankgroup")
@SpringBootTest(classes = {ConsorsbankGroupTestApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = {"classpath:/stubs/consorsbankgroup/ais-1.3/accounts-403"}, httpsPort = 0, port = 0)
class ConsorsbankGroupDataProviderAccountsHttp403IntegrationTest {

    private static final String CONSENT_EXTERNAL_ID = "http403";
    private static final String PSU_IP_ADDRESS = "TEST_PSU_IP_ADDRESS";

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Autowired
    private ConsorsbankGroupDataProvider dataProvider;

    @Autowired
    @Qualifier("consorsbankGroupObjectMapper")
    private ObjectMapper objectMapper;

    private RestTemplateManager restTemplateManager;

    @BeforeEach
    public void beforeEach() {
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }


    @Test
    public void shouldThrowTokenInvalidExceptionWhenFetchingTheDataWithExpiredConsent() throws IOException, URISyntaxException {
        // given
        UUID testUserId = UUID.randomUUID();
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(sampleAccessMeansDTO(CONSENT_EXTERNAL_ID, testUserId))
                .setAuthenticationMeans(ConsorsbankGroupSampleAuthMeans.sampleAuthMeans())
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> dataProvider.fetchData(request);

        // then
        Assertions.assertThatThrownBy(throwingCallable).isExactlyInstanceOf(TokenInvalidException.class)
                .hasMessage("Access to call is forbidden: HTTP 403");
    }

    @SneakyThrows
    private AccessMeansDTO sampleAccessMeansDTO(String consentId, UUID userId) {
        DefaultAccessMeans accessMeans = new DefaultAccessMeans(consentId);
        String serializedAccessMeans = objectMapper.writeValueAsString(accessMeans);
        return new AccessMeansDTO(
                userId,
                serializedAccessMeans,
                new Date(),
                Date.from(Instant.now().plus(89, ChronoUnit.DAYS)));
    }
}