package com.yolt.providers.openbanking.ais.tescobank.ais.v3.v2;

import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.tescobank.TescoBankApp;
import com.yolt.providers.openbanking.ais.tescobank.TescoSampleTypedAuthenticationMeansV2;
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
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains case according to documentation, when request to remove consent on bank side fails, because of
 * unexpected error. We want to try again in te future so throw {@link HttpServerErrorException}
 * <p>
 * Covered flows:
 * - deleting consent on bank side
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TescoBankApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/tescobank/ais-3.1/account-access-consents-delete-500", httpsPort = 0, port = 0)
@ActiveProfiles("tescobank")
public class TescoDataProviderDeleteUserSite500IntegrationTest {

    private static final Signer SIGNER = new SignerMock();
    private static final String REQUEST_TRACE_ID = UUID.randomUUID().toString();

    @Autowired
    @Qualifier("TescoBankDataProviderV7")
    private GenericBaseDataProviderV2 tescoBankDataProviderV7;

    private Stream<GenericBaseDataProviderV2> getProviders() {
        return Stream.of(tescoBankDataProviderV7);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldThrowHttpServerErrorExceptionInternalServerErrorWhenOnUserSiteDeleteFails(GenericBaseDataProviderV2 provider) throws IOException, URISyntaxException {
        // given
        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId("363ca7c1-9d03-4876-8766-ddefc9fd2d76")
                .setAuthenticationMeans(TescoSampleTypedAuthenticationMeansV2.getTypedAuthenticationMeans())
                .setRestTemplateManager(new RestTemplateManagerMock(() -> REQUEST_TRACE_ID))
                .setSigner(SIGNER)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> provider.onUserSiteDelete(urlGetLogin);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(HttpServerErrorException.InternalServerError.class);
    }
}
