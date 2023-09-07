package com.yolt.providers.openbanking.ais.cybgroup.ais.v3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.cybgroup.CybgGroupApp;
import com.yolt.providers.openbanking.ais.cybgroup.CybgGroupSampleAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.CybgGroupDataProviderV3;
import com.yolt.providers.openbanking.ais.cybgroup.common.model.CybgGroupAccessMeansV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount4Account;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountType1Code;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains case according to documentation, when request to refresh token is called without refresh token value.
 * This means that this operation is not possible, thus we want to force user to fill a consent (so throw {@link TokenInvalidException})
 * <p>
 * Disclaimer: all in CYBG group are the same from code and stubs perspective (then only difference is configuration)
 * Due to that fact this test class is parametrised, so all providers in group are tested.
 * <p>
 * Covered flows:
 * - refreshing access means
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {CybgGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("cybgroup")
public class CybgGroupDataProviderV3RefreshTokenMissingRefreshTokenIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REDIRECT_URL = "http://yolt.com/identifier";

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private static final Signer SIGNER = new SignerMock();

    private String requestTraceId;
    private final RestTemplateManager restTemplateManager = new RestTemplateManagerMock(() -> requestTraceId);

    private final CybgGroupSampleAuthenticationMeansV2 sampleAuthenticationMeans = new CybgGroupSampleAuthenticationMeansV2();
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("ClydesdaleDataProvider")
    private CybgGroupDataProviderV3 clydesdaleDataProvider;

    @Autowired
    @Qualifier("YorkshireDataProvider")
    private CybgGroupDataProviderV3 yorkshireDataProviderV3;

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(clydesdaleDataProvider, yorkshireDataProviderV3);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        requestTraceId = "4bf28754-9c17-41e6-bc46-6cf98fff679";
        authenticationMeans = sampleAuthenticationMeans.getCybgGroupSampleAuthenticationMeansForAis();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldThrowTokenInvalidExceptionWhenRefreshTokenIsNull(UrlDataProvider provider) throws JsonProcessingException {
        // given
        UrlRefreshAccessMeansRequest refreshRequest = createUrlRefreshAccessMeansRequest();

        // when
        ThrowableAssert.ThrowingCallable refreshTokenCallable = () -> provider.refreshAccessMeans(refreshRequest);

        // then
        assertThatThrownBy(refreshTokenCallable)
                .isExactlyInstanceOf(TokenInvalidException.class)
                .hasMessage("Refresh token is missing, and access token is expired.");
    }

    private UrlRefreshAccessMeansRequest createUrlRefreshAccessMeansRequest() throws JsonProcessingException {
        return new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(createAccessMeansDTOWithMissingRefreshToken(Collections.singletonList(createProviderAccountDTO())))
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .setRestTemplateManager(restTemplateManager)
                .build();
    }

    private OBAccount6 createProviderAccountDTO() {
        OBAccount6 account = new OBAccount6();
        account.setCurrency("EUR");
        account.setNickname("Test Account");
        account.setAccountSubType(OBExternalAccountSubType1Code.CURRENTACCOUNT);
        account.setAccountId("92198f42-4726-12fd-ad55-b93b04192121");
        account.setAccountType(OBExternalAccountType1Code.PERSONAL);
        OBAccount4Account accountNumber = new OBAccount4Account();
        accountNumber.setSchemeName("UK.OBIE.IBAN");
        accountNumber.setIdentification("IT35 5000 0000 0549 1000 0003");
        account.setAccount(List.of(accountNumber));
        return account;
    }

    private AccessMeansDTO createAccessMeansDTOWithMissingRefreshToken(List<OBAccount6> accounts) throws JsonProcessingException {
        AccessMeans accessMeans = new AccessMeans(Instant.now(), USER_ID, ACCESS_TOKEN, null, getExpirationDate(), new Date(), REDIRECT_URL);
        String providerState = objectMapper.writeValueAsString(new CybgGroupAccessMeansV2(accessMeans, accounts));
        return new AccessMeansDTO(USER_ID, providerState, new Date(), getExpirationDate());
    }

    private Date getExpirationDate() {
        return Date.from(Instant.now().plus(1, DAYS));
    }
}
