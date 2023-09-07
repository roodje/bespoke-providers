package com.yolt.providers.openbanking.ais.monzogroup;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.Provider;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
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

import static com.yolt.providers.openbanking.ais.monzogroup.common.auth.MonzoGroupAuthMeansMapper.*;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {MonzoApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("monzogroup")
public class MonzoProviderAuthMeansIntegrationTest {

    @Autowired
    @Qualifier("MonzoDataProviderV5")
    private GenericBaseDataProvider monzoDataProviderV5;

    @Autowired
    @Qualifier("MonzoPaymentProviderV5")
    private GenericBasePaymentProviderV2 monzoPaymentProviderV5;

    private Stream<Provider> getMonzoProviders() {
        return Stream.of(monzoDataProviderV5, monzoPaymentProviderV5);
    }

    @ParameterizedTest
    @MethodSource("getMonzoProviders")
    public void shouldReturnTransportKeyRequirements(Provider provider) {
        // when
        KeyRequirements transportKeyRequirements = provider.getTransportKeyRequirements().get();
        // then
        assertThat(transportKeyRequirements).isEqualTo(
                HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getMonzoProviders")
    public void shouldReturnSigningKeyRequirements(Provider provider) {
        // when
        KeyRequirements signingKeyRequirements = provider.getSigningKeyRequirements().get();
        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getMonzoProviders")
    public void shouldReturnTypedAuthenticationMeans(Provider provider) {
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
                SOFTWARE_STATEMENT_ASSERTION_NAME,
                SOFTWARE_ID_NAME,
                ORGANIZATION_ID_NAME);
    }


    @ParameterizedTest
    @MethodSource("getMonzoProviders")
    public void shouldReturnAutoConfiguredTypedAuthenticationMeans(AutoOnboardingProvider provider) {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = provider.getAutoConfiguredMeans();
        // then
        assertThat(authenticationMeans).containsOnlyKeys(CLIENT_ID_NAME);
    }
}
