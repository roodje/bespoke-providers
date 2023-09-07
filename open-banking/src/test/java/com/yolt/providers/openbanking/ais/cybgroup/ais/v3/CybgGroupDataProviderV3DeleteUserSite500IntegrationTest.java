package com.yolt.providers.openbanking.ais.cybgroup.ais.v3;

import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.cybgroup.CybgGroupApp;
import com.yolt.providers.openbanking.ais.cybgroup.CybgGroupSampleAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.CybgGroupDataProviderV3;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains case according to documentation, when request to remove consent on bank side fails, because of
 * unexpected error. We want to try again in te future so throw {@link HttpServerErrorException}
 * <p>
 * Disclaimer: all in CYBG group are the same from code and stubs perspective (then only difference is configuration)
 * Due to that fact this test class is parametrised, so all providers in group are tested.
 * <p>
 * Covered flows:
 * - deleting consent on bank side
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {CybgGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/cybgroup/ais-3.1/account-access-consents-delete-500", httpsPort = 0, port = 0)
@ActiveProfiles("cybgroup")
public class CybgGroupDataProviderV3DeleteUserSite500IntegrationTest {

    private static final Signer SIGNER = new SignerMock();

    private final String requestTraceId = "4bf28754-9c17-41e6-bc46-6cf98fff679";

    @Autowired
    @Qualifier("ClydesdaleDataProvider")
    private CybgGroupDataProviderV3 clydesdaleDataProvider;

    @Autowired
    @Qualifier("YorkshireDataProvider")
    private CybgGroupDataProviderV3 yorkshireDataProviderV3;

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(clydesdaleDataProvider, yorkshireDataProviderV3);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldDeleteUserSiteThrowHttpServerErrorExceptionWhenResponseStatusIs500(UrlDataProvider provider) throws IOException, URISyntaxException {
        // given
        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId("3366f720-26a7-11e8-b65a-bd9397faa378")
                .setAuthenticationMeans(new CybgGroupSampleAuthenticationMeansV2().getCybgGroupSampleAuthenticationMeansForAis())
                .setRestTemplateManager(new RestTemplateManagerMock(() -> requestTraceId))
                .setSigner(SIGNER)
                .build();

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> provider.onUserSiteDelete(urlGetLogin);

        // then
        assertThatThrownBy(onUserSiteDeleteCallable)
                .isExactlyInstanceOf(HttpServerErrorException.InternalServerError.class)
                .hasMessage("500 Server Error: [no body]");
        ;
    }
}
