package com.yolt.providers.openbanking.ais.tescobank;

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

import java.util.Map;
import java.util.stream.Stream;

import static com.yolt.providers.openbanking.ais.tescobank.auth.TescoBankAuthMeansBuilderV3.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {TescoBankApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TescoBankProviderTest {

    @Autowired
    @Qualifier("TescoBankDataProviderV7")
    private GenericBaseDataProviderV2 tescoBankDataProviderV7;

    @Autowired
    @Qualifier("TescoBankPaymentProviderV5")
    private GenericBasePaymentProviderV2 tescoBankPaymentProviderV4;

    private Stream<Provider> getTescoProviders() {
        return Stream.of(tescoBankDataProviderV7, tescoBankPaymentProviderV4);
    }

    private Stream<Provider> getTescoBankDataProviders() {
        return Stream.of(tescoBankDataProviderV7);
    }

    private Stream<Provider> getTescoBankPaymentProviders() {
        return Stream.of(tescoBankPaymentProviderV4);
    }

    @ParameterizedTest
    @MethodSource("getTescoProviders")
    public void shouldReturnTransportKeyRequirements(Provider provider) {
        // when
        KeyRequirements transportKeyRequirements = provider.getTransportKeyRequirements().get();
        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getTescoProviders")
    public void shouldReturnSigningKeyRequirements(Provider provider) {
        // when
        KeyRequirements signingKeyRequirements = provider.getSigningKeyRequirements().get();
        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getTescoBankDataProviders")
    public void shouldReturnTypedAuthenticationMeansForAIS(Provider provider) {
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
                ORGANIZATION_ID_NAME,
                SOFTWARE_ID_NAME);
    }

    @ParameterizedTest
    @MethodSource("getTescoBankPaymentProviders")
    public void shouldReturnTypedAuthenticationMeansForPIS(Provider provider) {
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
                ORGANIZATION_ID_NAME,
                SOFTWARE_ID_NAME);
    }
}
