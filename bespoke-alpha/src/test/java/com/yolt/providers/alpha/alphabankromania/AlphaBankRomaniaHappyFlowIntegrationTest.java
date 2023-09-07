package com.yolt.providers.alpha.alphabankromania;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.alpha.SignerMock;
import com.yolt.providers.alpha.common.AlphaDataProvider;
import com.yolt.providers.alpha.common.auth.dto.AlphaToken;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeAll;
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
import java.time.Clock;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = AlphaBankRomaniaTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("alpha")
@AutoConfigureWireMock(stubs = "classpath:/stubs/happyflow", port = 0, httpsPort = 0)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AlphaBankRomaniaHappyFlowIntegrationTest {

    public static final String REFRESH_TOKEN = "8c78cb47a647421e96aa1a1a86d9c4af45b36f61c47f41fdb7bc6ffded6f9613eab52024dfc04abf94e1e78ec7ff213743f5e8aa822745a38474feb132454b48";
    public static final Long TOKEN_EXPIRAION_TIME_IN_SEC = 3599L;
    public static final String BEARER = "bearer";
    private static final Signer SIGNER = new SignerMock();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJsYW5nIjoiZWwiLCJuYW1lIjoiZ3BhcGFkb3BvdWxvcyIsInNjb3BlIjoiYWNjb3VudC1pbmZvIiwiY2xpZW50X2lkIjoiMjVjNDQ1ZDAyYWM2NDdhNjg4MjVlZDQ5MWNkODQ5ODciLCJyZXF1ZXN0IjoiMzUzNjA4OWMtMjY2Zi00MzA2LTk4NGYtMTUyMzNkNzExODAwIiwic2Vzc2lvbl9pZCI6IjM2NzhmZWRmLWMyMTctNDNmMS1iM2UyLTc1ZDEzZDRkMjNmZiIsInRva2VuX2lkIjoiZWEzZjg1YjAtNDU1Ny00YzhlLWI3ZmQtNGQ4NmEyYjY2MjBhIiwia2lkIjoiRDZCRjk0NzFEQ0I4MkUyMDk1NERFRUEwNUVGQ0M1NzU4QUE0MkJEQiIsIm5iZiI6MTYyMDc5ODk2NywiZXhwIjoxNjIwODAyNTY3LCJpc3MiOiJBbHBoYWJhbmsgUFNEMiBPYXV0aCBTZXJ2ZXIiLCJhdWQiOiJhY2NvdW50LWluZm8ifQ.O7P_CylcqY2Dd5pD3GpzR2vfizro8dS3gNWuQVneb-Mjf2f-ZvAMUk6InyvLhNx7_xyy1qZE6trgWbWBOI76PcA5CeuclolLrmO6NjUZ2AJSwxfPHtYN2wIwq1PArAzgrG0b2f5TMxiyNAm0i1SqU87FHng23it-c1SF0wtDyea54Xf6_99PEZyUX2ZzBGIGwl_dNieD9ObG1LeLJl_FPt0YBOhT1CfP9yFv4J5zzIvK__EuEwLuuIaB_cTMEp2DP_8TP_wlcUEpNvrSHq4sSiCcBoOvlQNT6VmBjVDb_y06iHmuUTr2uE72FC82iTuAZCzY7zTC4xElSmnzZ_tj7g";
    private static final UUID TEST_USER_ID = UUID.randomUUID();
    @Autowired
    private RestTemplateManager restTemplateManagerMock;
    @Autowired
    private Clock clock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("AlphaBankDataProviderV1")
    private AlphaDataProvider alphaBankDataProviderV1;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(alphaBankDataProviderV1);
    }

    @BeforeAll
    public void beforeAll() throws IOException, URISyntaxException {
        authenticationMeans = new AlphaSampleAuthenticationMeans().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnProperLoginUrl(UrlDataProvider dataProvider) {
        //given
        String state = UUID.randomUUID().toString();
        String baseClientRedirectUrl = "http://yolt.com/identifier";
        UrlGetLoginRequest urlGetLoginRequest = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(baseClientRedirectUrl)
                .setState(state)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();
        String expectedUrlRegex = ".*/auth/authorize\\?client_id=client_id&response_type=code&scope=account-info&redirect_uri="
                + baseClientRedirectUrl + "&state=" + state + "&request=3536089c-266f-4306-984f-15233d711800";
        //when
        RedirectStep redirectStep = (RedirectStep) dataProvider.getLoginInfo(urlGetLoginRequest);
        //then
        assertThat(redirectStep.getRedirectUrl()).matches(expectedUrlRegex);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldCreateNewAccessMeansAndReturnThemInAccessMeansOrStepDTO(UrlDataProvider dataProvider) throws JsonProcessingException {
        //given
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(TEST_USER_ID)
                .setRedirectUrlPostedBackFromSite("http://yolt.com/identifier?state=state&code=exchange-code")
                .setBaseClientRedirectUrl("redirect-uri")
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();
        String expectedAccessMeans = OBJECT_MAPPER.writeValueAsString(new AlphaToken(
                ACCESS_TOKEN,
                REFRESH_TOKEN,
                BEARER,
                TOKEN_EXPIRAION_TIME_IN_SEC)
        );
        //when
        AccessMeansDTO response = dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest).getAccessMeans();
        //then
        assertThat(response).isEqualTo(new AccessMeansDTO(
                TEST_USER_ID,
                expectedAccessMeans,
                new Date(clock.millis()),
                new Date(clock.millis() + (TOKEN_EXPIRAION_TIME_IN_SEC * 1000))));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldRefreshAccessMeansAndReturnAccessMeansDTO(UrlDataProvider dataProvider) throws TokenInvalidException, JsonProcessingException {
        //given
        AlphaToken accessMeans = new AlphaToken(ACCESS_TOKEN, "refresh-token", BEARER, TOKEN_EXPIRAION_TIME_IN_SEC);
        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(TEST_USER_ID, OBJECT_MAPPER.writeValueAsString(accessMeans), new Date(clock.millis()), new Date(clock.millis()))
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();
        String expectedAccessMeans = OBJECT_MAPPER.writeValueAsString(new AlphaToken(
                ACCESS_TOKEN,
                REFRESH_TOKEN,
                BEARER,
                TOKEN_EXPIRAION_TIME_IN_SEC)
        );
        //when
        AccessMeansDTO response = dataProvider.refreshAccessMeans(request);
        //then
        assertThat(response).isEqualTo(new AccessMeansDTO(
                TEST_USER_ID,
                expectedAccessMeans,
                new Date(clock.millis()),
                new Date(clock.millis() + (TOKEN_EXPIRAION_TIME_IN_SEC * 1000))));
    }
}
