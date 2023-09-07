package com.yolt.providers.openbanking.ais.bankofirelandgroup.ais;

import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.BankOfIrelandGroupApp;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.BankOfIrelandRoiSampleTypedAuthMeans;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.BankOfIrelandSampleTypedAuthMeans;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.BankOfIrelandGroupBaseDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains case according to documentation, when request to remove consent on bank side fails, because user
 * is unauthorized. This means that we have to force user to fill a consent (so throw {@link TokenInvalidException})
 * <p>
 * Covered flows:
 * - deleting consent on bank side
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BankOfIrelandGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/bankofireland/ais-3.0.0/account-access-consents-delete-403", httpsPort = 0, port = 0)
@ActiveProfiles("bankofireland")
public class BankOfIrelandGroupDataProviderDeleteUserSite403IntegrationTest {

    private static final Signer SIGNER = new SignerMock();

    private RestTemplateManagerMock restTemplateManagerMock;
    private String requestTraceId;

    @Autowired
    @Qualifier("BankOfIrelandDataProviderV7")
    private BankOfIrelandGroupBaseDataProvider bankOfIrelandDataProviderV7;

    @Autowired
    @Qualifier("BankOfIrelandRoiDataProvider")
    private BankOfIrelandGroupBaseDataProvider bankOfIrelandRoiDataProvider;

    private Stream<Arguments> getProvidersWithSampleAuthMeans() {
        return Stream.of(
                Arguments.of(bankOfIrelandDataProviderV7, BankOfIrelandSampleTypedAuthMeans.getSampleAuthMeans()),
                Arguments.of(bankOfIrelandRoiDataProvider, BankOfIrelandRoiSampleTypedAuthMeans.getSampleAuthMeans())
        );
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        requestTraceId = "5008e82d-f5ac-42fe-8f07-a49b025f3c2e";
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithSampleAuthMeans")
    public void shouldThrowTokenInvalidExceptionWhenUnauthorizedCallToOnUserSiteDeleteIsMade(BankOfIrelandGroupBaseDataProvider subject, Map<String, BasicAuthenticationMean> authenticationMeans) throws TokenInvalidException {
        // given
        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId("50ca5ed5-317c-451c-8438-3b3fb91466e1")
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        // when
        ThrowableAssert.ThrowingCallable handler = () -> subject.onUserSiteDelete(urlGetLogin);

        // then
        assertThatThrownBy(handler).isExactlyInstanceOf(TokenInvalidException.class);
    }
}