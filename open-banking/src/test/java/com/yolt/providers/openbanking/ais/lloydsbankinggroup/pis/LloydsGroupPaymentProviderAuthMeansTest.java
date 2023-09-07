package com.yolt.providers.openbanking.ais.lloydsbankinggroup.pis;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.auth.LloydsBankingGroupAuthenticationMeansV3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.auth.LloydsBankingGroupAuthenticationMeansV3.*;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LloydsGroupPaymentProviderAuthMeansTest {

    private GenericBasePaymentProviderV2 lbgPaymentProvider = new GenericBasePaymentProviderV2(null,
            null,
            null,
            null,
            LloydsBankingGroupAuthenticationMeansV3.getTypedAuthenticationMeansForPis(),
            () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
            () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
            null);

    private Stream<GenericBasePaymentProviderV2> getProviders() {
        return Stream.of(lbgPaymentProvider);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnTransportKeyRequirements(GenericBasePaymentProviderV2 provider) {
        // when
        KeyRequirements transportKeyRequirements = provider.getTransportKeyRequirements().get();

        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME,
                TRANSPORT_CERTIFICATE_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnSigningKeyRequirements(GenericBasePaymentProviderV2 provider) {
        // when
        KeyRequirements signingKeyRequirements = provider.getSigningKeyRequirements().get();

        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME).get());
    }

    @Test
    public void shouldReturnTypedAuthenticationMeans() {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = lbgPaymentProvider.getTypedAuthenticationMeans();

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
