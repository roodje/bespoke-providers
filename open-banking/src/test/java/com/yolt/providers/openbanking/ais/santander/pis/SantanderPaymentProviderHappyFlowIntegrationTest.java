package com.yolt.providers.openbanking.ais.santander.pis;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.santander.SantanderApp;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.stream.Stream;

import static com.yolt.providers.openbanking.ais.santander.auth.SantanderAuthMeansMapper.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all payment happy flows occuring in Santander.
 * <p>
 * Disclaimer: Santander is a single bank, so there is no need to parametrize this test class.
 * <p>
 * Covered flows:
 * - successful return of consent page url
 * - successful creation of payment
 * - successful confirmation of payment
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {SantanderApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = {"classpath:/stubs/santander/pis-3.1.19/happy-flow"}, httpsPort = 0, port = 0)
@ActiveProfiles("santander")
class SantanderPaymentProviderHappyFlowIntegrationTest {

    @Autowired
    @Qualifier("SantanderPaymentProviderV15")
    private GenericBasePaymentProviderV2 santanderPaymentProviderV15;

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldReturnTransportKeyRequirements(GenericBasePaymentProviderV2 provider) {
        // when
        KeyRequirements transportKeyRequirements = provider.getTransportKeyRequirements().get();
        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldReturnSigningKeyRequirements(GenericBasePaymentProviderV2 provider) {
        // when
        KeyRequirements signingKeyRequirements = provider.getSigningKeyRequirements().get();
        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldReturnTypedAuthenticationMeans(GenericBasePaymentProviderV2 provider) {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = provider.getTypedAuthenticationMeans();
        // then
        assertThat(authenticationMeans)
                .hasSize(8)
                .containsOnlyKeys(
                        INSTITUTION_ID_NAME,
                        CLIENT_ID_NAME,
                        PRIVATE_SIGNING_KEY_HEADER_ID_NAME,
                        SIGNING_PRIVATE_KEY_ID_NAME,
                        TRANSPORT_CERTIFICATE_NAME,
                        TRANSPORT_PRIVATE_KEY_ID_NAME,
                        ORGANIZATION_ID_NAME,
                        SOFTWARE_ID_NAME
                );
    }

    private Stream<GenericBasePaymentProviderV2> getPaymentProviders() {
        return Stream.of(santanderPaymentProviderV15);
    }
}