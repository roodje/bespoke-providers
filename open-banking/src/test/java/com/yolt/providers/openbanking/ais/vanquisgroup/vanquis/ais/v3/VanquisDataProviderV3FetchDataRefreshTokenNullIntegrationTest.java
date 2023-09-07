package com.yolt.providers.openbanking.ais.vanquisgroup.vanquis.ais.v3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequestBuilder;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.Provider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import com.yolt.providers.openbanking.ais.vanquisgroup.VanquisGroupApp;
import com.yolt.providers.openbanking.ais.vanquisgroup.VanquisGroupSampleTypedAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.VanquisGroupBaseDataProviderV2;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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

/**
 * This test contains case when refresh token is null.
 * This means that new access token can't be created, thus we want to force user to fill a consent (so throw {@link TokenInvalidException})
 * <p>
 * Disclaimer: Vanquis is a single bank, so there is no need to parametrize this test class.
 * <p>
 * Covered flows:
 * - refreshing access means
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {VanquisGroupApp.class, OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/vanquisgroup/ais/ob_3.1.1/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("vanquisgroupV1")
public class VanquisDataProviderV3FetchDataRefreshTokenNullIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static final SignerMock SIGNER = new SignerMock();

    @Autowired
    @Qualifier("VanquisDataProviderV3")
    private VanquisGroupBaseDataProviderV2 vanquisDataProvider;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new VanquisGroupSampleTypedAuthenticationMeansV2().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldThrowTokenInvalidExceptionWhenRefreshTokenIsNull(VanquisGroupBaseDataProviderV2 dataProvider) throws Exception {
        // given
        ObjectMapper objectMapper = OpenBankingTestObjectMapper.INSTANCE;
        Instant now = Instant.now();
        AccessMeans token = new AccessMeans(
                now,
                USER_ID,
                "accessToken",
                null,
                Date.from(now.plus(1, ChronoUnit.DAYS)),
                Date.from(now),
                TEST_REDIRECT_URL);
        String serializedOAuthToken = objectMapper.writeValueAsString(token);
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedOAuthToken, new Date(), new Date());
        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        // when -> then
        assertThatThrownBy(() -> dataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest))
                .isExactlyInstanceOf(TokenInvalidException.class);

    }

    private Stream<Provider> getDataProviders() {
        return Stream.of(vanquisDataProvider);
    }
}
