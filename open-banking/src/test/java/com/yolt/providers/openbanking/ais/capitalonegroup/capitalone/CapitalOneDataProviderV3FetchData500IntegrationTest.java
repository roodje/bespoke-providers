package com.yolt.providers.openbanking.ais.capitalonegroup.capitalone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.capitalonegroup.CapitalOneGroupApp;
import com.yolt.providers.openbanking.ais.capitalonegroup.CapitalOneGroupJwsSigningResult;
import com.yolt.providers.openbanking.ais.capitalonegroup.CapitalOneGroupSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.capitalonegroup.common.CapitalOneGroupDataProviderV3;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.SneakyThrows;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * This test contains case according to documentation, when request to accounts returned 500.
 * This means that bank doesn't respond on time, thus we want to inform user that fetch data failed (so throw {@link ProviderFetchDataException})
 * <p>
 * Covered flows:
 * - fetching data
 * <p>
 */
@SpringBootTest(classes = {CapitalOneGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/capitalonegroup/ais-3.1.2/accounts-500", httpsPort = 0, port = 0)
@ActiveProfiles("capitalonegroup")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CapitalOneDataProviderV3FetchData500IntegrationTest {

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://test-redirect-url.com/identifier";
    private static final String TEST_PSU_IP_ADDRESS = "1.1.1.1";
    private static final String TEST_ACCESS_TOKEN = "TEST_ACCESS_TOKEN";
    private static final String TEST_REFRESH_TOKEN = "TEST_REFRESH_TOKEN";
    private static final AccessMeans TEST_ACCESS_MEANS = new AccessMeans(
            Instant.now(),
            TEST_USER_ID,
            TEST_ACCESS_TOKEN,
            TEST_REFRESH_TOKEN,
            Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
            Date.from(Instant.now()),
            TEST_REDIRECT_URL);

    private static final Map<String, BasicAuthenticationMean> TEST_AUTHENTICATION_MEANS = CapitalOneGroupSampleAuthenticationMeans.getSampleAuthenticationMeans();
    private static final RestTemplateManagerMock REST_TEMPLATE_MANAGER_MOCK = new RestTemplateManagerMock(() -> "35acdd5c-ddf1-4a70-ac0f-a4322e3bc263");

    @Mock
    private Signer signer;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("CapitalOneDataProviderV4")
    private CapitalOneGroupDataProviderV3 capitalOneDataProviderV4;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(capitalOneDataProviderV4);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        when(signer.sign(ArgumentMatchers.any(JsonWebSignature.class), any(), ArgumentMatchers.any(SignatureAlgorithm.class)))
                .thenReturn(new CapitalOneGroupJwsSigningResult());
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldThrowProviderFetchDataExceptionWhenAccountsRequestFail(UrlDataProvider provider) {
        // given
        UrlFetchDataRequest urlFetchDataRequest = createUrlFetchDataRequest();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> provider.fetchData(urlFetchDataRequest);

        // then
        assertThatThrownBy(fetchDataCallable)
                .isExactlyInstanceOf(ProviderFetchDataException.class)
                .hasMessage("Failed fetching data");
    }

    private UrlFetchDataRequest createUrlFetchDataRequest() {
        return new UrlFetchDataRequestBuilder()
                .setUserId(TEST_USER_ID)
                .setUserSiteId(null)
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(new AccessMeansDTO(TEST_USER_ID, toSerializedAccessMeans(), new Date(), new Date()))
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setSigner(signer)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER_MOCK)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .build();
    }

    @SneakyThrows
    private String toSerializedAccessMeans() {
        return objectMapper.writeValueAsString(TEST_ACCESS_MEANS);
    }
}
