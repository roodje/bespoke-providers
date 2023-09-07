package com.yolt.providers.openbanking.ais.nationwide;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.providerinterface.Provider;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProvider;
import com.yolt.providers.openbanking.ais.nationwide.auth.NationwideAuthMeansBuilderV3;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.yolt.providers.openbanking.ais.nationwide.auth.NationwideAuthMeansBuilderV3.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains data provider and payment provider tests for Nationwide.
 * <p>
 * Disclaimer: This is a parametrized test, because list of authentication means is the same for AIS and PIS providers.
 * <p>
 * Covered flows:
 * - transport key requirements test
 * - signing key requirements test
 * - get typed authentication means test
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NationwideProviderTest {

    private NationwideDataProviderV10 nationwideDataProviderV8 = new NationwideDataProviderV10(null,
            null,
            null,
            null,
            null,
            null,
            null,
            NationwideAuthMeansBuilderV3.getTypedAuthenticationMeansForAIS(),
            null,
            null,
            () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
            () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
            null,
            null,
            null,
            null,
            null);

    private GenericBasePaymentProvider nationwidePaymentProviderV12 = new GenericBasePaymentProvider(null,
            null,
            null,
            null,
            null,
            null,
            NationwideAuthMeansBuilderV3.getTypedAuthenticationMeansForPIS(),
            () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
            () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME));

    private Stream<Provider> getProviders() {
        return Stream.concat(getAisProviders(), getPisProviders());
    }

    private Stream<Provider> getAisProviders() {
        return Stream.of(nationwideDataProviderV8);
    }

    private Stream<Provider> getPisProviders() {
        return Stream.of(nationwidePaymentProviderV12);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnTransportKeyRequirements(Provider provider) {
        // when
        Optional<KeyRequirements> transportKeyRequirements = provider.getTransportKeyRequirements();

        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(NationwideAuthMeansBuilderV3.TRANSPORT_PRIVATE_KEY_ID_NAME, NationwideAuthMeansBuilderV3.TRANSPORT_CERTIFICATE_NAME));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnSigningKeyRequirements(Provider provider) {
        // when
        KeyRequirements signingKeyRequirements = provider.getSigningKeyRequirements().get();

        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(NationwideAuthMeansBuilderV3.SIGNING_PRIVATE_KEY_ID_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getAisProviders")
    public void shouldReturnTypedAuthenticationMeansForDataProviders(Provider provider) {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = provider.getTypedAuthenticationMeans();

        // then
        assertThat(authenticationMeans).containsOnlyKeys(
                INSTITUTION_ID_NAME,
                CLIENT_ID_NAME,
                SIGNING_KEY_HEADER_ID_NAME,
                SIGNING_PRIVATE_KEY_ID_NAME,
                TRANSPORT_CERTIFICATE_NAME,
                TRANSPORT_PRIVATE_KEY_ID_NAME,
                SOFTWARE_ID_NAME,
                ORGANIZATION_ID_NAME,
                SOFTWARE_STATEMENT_ASSERTION_NAME
        );
    }

    @ParameterizedTest
    @MethodSource("getPisProviders")
    public void shouldReturnTypedAuthenticationMeansForPaymentProviders(Provider provider) {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = provider.getTypedAuthenticationMeans();

        // then
        assertThat(authenticationMeans).containsOnlyKeys(
                INSTITUTION_ID_NAME,
                CLIENT_ID_NAME,
                SIGNING_KEY_HEADER_ID_NAME,
                SIGNING_PRIVATE_KEY_ID_NAME,
                TRANSPORT_CERTIFICATE_NAME,
                TRANSPORT_PRIVATE_KEY_ID_NAME,
                SOFTWARE_ID_NAME,
                ORGANIZATION_ID_NAME
        );
    }
}
