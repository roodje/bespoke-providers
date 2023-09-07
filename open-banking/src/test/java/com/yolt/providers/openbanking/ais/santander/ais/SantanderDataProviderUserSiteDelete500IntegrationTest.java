package com.yolt.providers.openbanking.ais.santander.ais;

import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderRequestFailedException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.santander.SantanderApp;
import com.yolt.providers.openbanking.ais.santander.SantanderSampleAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.santander.dto.SantanderAccessMeansV2;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeAll;
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
 * When there are some server issue on bank side, we want to retry the deletion in future,
 * thus we throw {@link com.yolt.providers.openbanking.ais.exception.ProviderRequestFailedException})
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {SantanderApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("santander")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/santander/ais-3.1.6/user-site-delete-500"}, httpsPort = 0, port = 0)
public class SantanderDataProviderUserSiteDelete500IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final Signer SIGNER = new SignerMock();

    private static SantanderAccessMeansV2 token;

    private RestTemplateManagerMock restTemplateManagerMock;
    private String requestTraceId;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("SantanderDataProviderV17")
    private GenericBaseDataProviderV2 santanderDataProviderV17;

    @BeforeAll
    public static void setup() {
        token = new SantanderAccessMeansV2(
                Instant.now(),
                USER_ID,
                "AAIkMDM5NDJmZTUtOGNiMi00NzVmLWIwMTItNDgyZjM0ZTExYzI58q0t070fBgubnd8pgwu3kCwNt91ZJhhW3wfUl2UulSRjiKcfWfQQ9J9i8OU2QOSciVIl8mQ69GO7mDZ0uEv8INrboRu4fesBmEMq7PS87O7LrN7isyqwzpjKXBZR2JJkL3nF10SuDt_l4SItojPO4",
                "qx3scq02pKLSkSJklsjDJwi8SJN82kSD44tGLSLKjsiojw89mDMUIHMDSIUyw89m2DuTlkCwRFxY0xSsKQuYAC6BinbvjksHMFIsihmsiuHMISUIW88w78SMJI8smjKMSJHKJSHMWIWSHIUGWUIgukwgjhskjshhkjsjkdhmsjkhdgshjhgsfsdfwefefwsefsegsdgsdfasjhguiynGUYFGU",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                "redirect");
        token.setCreated(Instant.now());
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        requestTraceId = "1626df30-50ad-42d8-8f39-40dd95f4b15f";
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        authenticationMeans = new SantanderSampleAuthenticationMeansV2().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldThrowProviderRequestFailedException(UrlDataProvider provider) {
        // given
        String externalConsentId = "ae24a1ae-61a4-11e9-8647-d663bd873d93";
        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId(externalConsentId)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> provider.onUserSiteDelete(urlGetLogin);

        // then
        assertThatThrownBy(fetchDataCallable)
                .isExactlyInstanceOf(ProviderRequestFailedException.class);
    }

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(santanderDataProviderV17);
    }
}
