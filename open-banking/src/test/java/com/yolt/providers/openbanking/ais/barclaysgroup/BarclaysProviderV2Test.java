package com.yolt.providers.openbanking.ais.barclaysgroup;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.providerinterface.Provider;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.stream.Stream;

import static com.yolt.providers.openbanking.ais.barclaysgroup.common.auth.BarclaysGroupAuthMeansBuilderV3.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains data provider and payment provider tests for Barclays.
 * <p>
 * Disclaimer: This is a parametrized test, because list of authentication means is the same for AIS and PIS providers.
 * <p>
 * Covered flows:
 * - transport key requirements test
 * - signing key requirements test
 * - get typed authentication means test
 */
@SpringBootTest(classes = {BarclaysApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("barclays")
public class BarclaysProviderV2Test {

    @Autowired
    @Qualifier("BarclaysPaymentProviderV16")
    private GenericBasePaymentProviderV2 barclaysPaymentProviderV16;

    @Autowired
    @Qualifier("BarclaysDataProviderV16")
    private GenericBaseDataProviderV2 barclaysDataProviderV16;
    
    private Stream<Provider> getProviders() {
        return Stream.of(barclaysPaymentProviderV16, barclaysDataProviderV16);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnTransportKeyRequirements(Provider provider) {
        // when
        KeyRequirements transportKeyRequirements = provider.getTransportKeyRequirements().get();
        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME_V2, TRANSPORT_CERTIFICATE_NAME_V2).get());
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnSigningKeyRequirements(Provider provider) {
        // when
        KeyRequirements signingKeyRequirements = provider.getSigningKeyRequirements().get();
        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME_V2).get());
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnTypedAuthenticationMeans(Provider provider) {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = provider.getTypedAuthenticationMeans();
        // then
        assertThat(authenticationMeans).containsOnlyKeys(
                INSTITUTION_ID_NAME_V2,
                CLIENT_ID_NAME_V2,
                PRIVATE_SIGNING_KEY_HEADER_ID_NAME_V2,
                SIGNING_PRIVATE_KEY_ID_NAME_V2,
                TRANSPORT_CERTIFICATE_NAME_V2,
                TRANSPORT_PRIVATE_KEY_ID_NAME_V2,
                SOFTWARE_ID_NAME_V2,
                ORGANIZATION_ID_NAME_V2
        );
    }
}

