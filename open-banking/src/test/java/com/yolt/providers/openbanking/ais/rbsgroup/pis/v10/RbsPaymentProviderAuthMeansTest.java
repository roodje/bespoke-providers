package com.yolt.providers.openbanking.ais.rbsgroup.pis.v10;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.rbsgroup.natwest.NatWestPropertiesV2;
import com.yolt.providers.openbanking.ais.rbsgroup.natwest.beanconfig.NatWestBeanConfigV2;
import com.yolt.providers.openbanking.ais.rbsgroup.royalbankofscotland.RoyalBankOfScotlandPropertiesV2;
import com.yolt.providers.openbanking.ais.rbsgroup.royalbankofscotland.beanconfig.RoyalBankOfScotlandBeanConfigV2;
import com.yolt.providers.openbanking.ais.rbsgroup.ulsterbank.UlsterBankPropertiesV2;
import com.yolt.providers.openbanking.ais.rbsgroup.ulsterbank.beanconfig.UlsterBankBeanConfigV2;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static com.yolt.providers.openbanking.ais.rbsgroup.common.auth.RbsGroupAuthMeansBuilderV4.*;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RbsPaymentProviderAuthMeansTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private GenericBasePaymentProviderV2 natWestPaymentProvider = new NatWestBeanConfigV2().getNatWestPaymentProviderV11(new NatWestPropertiesV2(), null, null, OBJECT_MAPPER);
    private GenericBasePaymentProviderV2 royalBankOfScotlandPaymentProvider = new RoyalBankOfScotlandBeanConfigV2().getRoyalBankOfScotlandPaymentProviderV11(new RoyalBankOfScotlandPropertiesV2(), null, null, OBJECT_MAPPER);
    private GenericBasePaymentProviderV2 ulsterBankPaymentProvider = new UlsterBankBeanConfigV2().getUlsterBankPaymentProviderV10(new UlsterBankPropertiesV2(), null, null, OBJECT_MAPPER);

    private Stream<GenericBasePaymentProviderV2> getProviders() {
        return Stream.of(natWestPaymentProvider, royalBankOfScotlandPaymentProvider, ulsterBankPaymentProvider);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnTransportKeyRequirements(GenericBasePaymentProviderV2 provider) {
        // when
        KeyRequirements transportKeyRequirements = provider.getTransportKeyRequirements().get();
        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnSigningKeyRequirements(GenericBasePaymentProviderV2 provider) {
        // when
        KeyRequirements signingKeyRequirements = provider.getSigningKeyRequirements().get();
        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnTypedAuthenticationMeans(GenericBasePaymentProviderV2 provider) {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = provider.getTypedAuthenticationMeans();
        // then
        assertThat(authenticationMeans).containsOnlyKeys(
                INSTITUTION_ID_NAME,
                CLIENT_ID_NAME,
                SIGNING_KEY_HEADER_ID_NAME,
                TRANSPORT_CERTIFICATES_CHAIN_NAME,
                SOFTWARE_ID_NAME,
                SOFTWARE_STATEMENT_ASSERTION_NAME,
                ORGANIZATION_ID_NAME,
                SIGNING_PRIVATE_KEY_ID_NAME,
                TRANSPORT_PRIVATE_KEY_ID_NAME
        );
    }
}
