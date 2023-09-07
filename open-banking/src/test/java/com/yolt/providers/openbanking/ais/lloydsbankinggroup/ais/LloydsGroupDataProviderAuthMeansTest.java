package com.yolt.providers.openbanking.ais.lloydsbankinggroup.ais;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
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
public class LloydsGroupDataProviderAuthMeansTest {

    private GenericBaseDataProvider lloydsBankingGroupDataProviderV4 =
            new GenericBaseDataProvider(null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    LloydsBankingGroupAuthenticationMeansV3.getTypedAuthenticationMeansForAIS(),
                    null,
                    () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                    () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                    null);

    private Stream<GenericBaseDataProvider> getProviders() {
        return Stream.of(lloydsBankingGroupDataProviderV4);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnTransportKeyRequirements(GenericBaseDataProvider provider) {
        // when
        KeyRequirements transportKeyRequirements = provider.getTransportKeyRequirements().get();
        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME,
                TRANSPORT_CERTIFICATE_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnSigningKeyRequirements(GenericBaseDataProvider provider) {
        // when
        KeyRequirements signingKeyRequirements = provider.getSigningKeyRequirements().get();
        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME).get());
    }

    @Test
    public void shouldReturnTypedAuthenticationMeans() {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = lloydsBankingGroupDataProviderV4.getTypedAuthenticationMeans();
        // then
        assertThat(authenticationMeans).containsOnlyKeys(
                INSTITUTION_ID_NAME,
                CLIENT_ID_NAME,
                SIGNING_KEY_HEADER_ID_NAME,
                SIGNING_PRIVATE_KEY_ID_NAME,
                TRANSPORT_CERTIFICATE_NAME,
                TRANSPORT_PRIVATE_KEY_ID_NAME
        );
    }
}
