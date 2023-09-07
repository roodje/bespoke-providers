package com.yolt.providers.knabgroup.knab.v2.sadflow.fetchData.consentExpiredDuringFetchData;

import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.knabgroup.TestApp;
import com.yolt.providers.knabgroup.TestRestTemplateManager;
import com.yolt.providers.knabgroup.TestSigner;
import com.yolt.providers.knabgroup.common.KnabGroupDataProviderV2;
import com.yolt.providers.knabgroup.samples.SampleAuthenticationMeans;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/sadflowv2/fetchdata/consentexpired", httpsPort = 0, port = 0)
class ConsentExpiredDuringFetchDataV2IntegrationTest {

    private static final String PSU_IP_ADDRESS = "0.0.0.0";

    private static final UUID USER_ID = UUID.fromString("fdbc609b-ec60-4ddf-a19a-5223c8b5b100");

    private static final String EXPECTED_ERROR_MESSAGE = "We are not authorized to call endpoint: HTTP 401";

    private static final String SERIALIZED_ACCESS_MEANS = "{\"accessToken\": \"userAccessToken\",\"refreshToken\": \"userRefreshToken\",\"tokenType\": \"Bearer\",\"expiryTimestamp\": 1595848039000,\"scope\": \"psd2 offline_access AIS:userConsentId\"}";

    private static final Date UNUSED_DATE_IN_PROVIDERS_SERVICE = new Date();
    private static final AccessMeansDTO ACCESS_MEANS_DTO = new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS, UNUSED_DATE_IN_PROVIDERS_SERVICE, UNUSED_DATE_IN_PROVIDERS_SERVICE);

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Autowired
    @Qualifier("KnabDataProviderV2")
    private KnabGroupDataProviderV2 provider;

    private RestTemplateManager restTemplateManager;
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private Signer signer;

    @BeforeEach
    public void beforeEach() {
        restTemplateManager = new TestRestTemplateManager(externalRestTemplateBuilderFactory);
        authenticationMeans = SampleAuthenticationMeans.getSampleAuthenticationMeans();
        signer = new TestSigner();
    }

    @Test
    void shouldThrowTokenInvalidExceptionAfter401FromServer() {
        // given
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(ACCESS_MEANS_DTO)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> provider.fetchData(urlFetchData);

        // then
        assertThatExceptionOfType(TokenInvalidException.class).isThrownBy(fetchDataCallable)
                .withMessage(EXPECTED_ERROR_MESSAGE);
    }

}
