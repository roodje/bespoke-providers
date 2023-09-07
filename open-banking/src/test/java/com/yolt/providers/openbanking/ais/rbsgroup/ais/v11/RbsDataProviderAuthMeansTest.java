package com.yolt.providers.openbanking.ais.rbsgroup.ais.v11;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.rbsgroup.common.RbsGroupDataProviderV5;
import com.yolt.providers.openbanking.ais.rbsgroup.common.auth.RbsGroupAuthMeansBuilderV4;
import com.yolt.providers.openbanking.ais.rbsgroup.natwest.NatWestPropertiesV2;
import com.yolt.providers.openbanking.ais.rbsgroup.natwest.beanconfig.NatWestBeanConfigV2;
import com.yolt.providers.openbanking.ais.rbsgroup.natwestcorporate.NatWestCorporatePropertiesV2;
import com.yolt.providers.openbanking.ais.rbsgroup.natwestcorporate.beanconfig.NatWestCorporateBeanConfigV2;
import com.yolt.providers.openbanking.ais.rbsgroup.royalbankofscotland.RoyalBankOfScotlandPropertiesV2;
import com.yolt.providers.openbanking.ais.rbsgroup.royalbankofscotland.beanconfig.RoyalBankOfScotlandBeanConfigV2;
import com.yolt.providers.openbanking.ais.rbsgroup.royalbankofscotlandcorporate.RoyalBankOfScotlandCorporatePropertiesV2;
import com.yolt.providers.openbanking.ais.rbsgroup.royalbankofscotlandcorporate.beanconfig.RoyalBankOfScotlandCorporateBeanConfigV2;
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
public class RbsDataProviderAuthMeansTest {

    private RbsGroupDataProviderV5 natWestDataProviderV11 = new NatWestBeanConfigV2().getNatWestDataProviderV11(new NatWestPropertiesV2(), null, null, null);
    private RbsGroupDataProviderV5 natWestCorporateDataProviderV10 = new NatWestCorporateBeanConfigV2().getNatWestCorporateDataProviderV10(new NatWestCorporatePropertiesV2(), null, null, null);
    private RbsGroupDataProviderV5 royalBankOfScotlandDataProviderV11 = new RoyalBankOfScotlandBeanConfigV2().getRoyalBankOfScotlandDataProviderV11(new RoyalBankOfScotlandPropertiesV2(), null, null, null);
    private RbsGroupDataProviderV5 royalBankOfScotlandCorporateDataProviderV10 = new RoyalBankOfScotlandCorporateBeanConfigV2().getRoyalBankOfScotlandCorporateDataProviderV10(new RoyalBankOfScotlandCorporatePropertiesV2(), null, null, null);
    private RbsGroupDataProviderV5 ulsterBankDataProviderV10 = new UlsterBankBeanConfigV2().getUlsterBankDataProviderV10(new UlsterBankPropertiesV2(), null, null, null);

    private Stream<GenericBaseDataProvider> getProviders() {
        return Stream.of(
                natWestDataProviderV11,
                natWestCorporateDataProviderV10,
                royalBankOfScotlandDataProviderV11,
                royalBankOfScotlandCorporateDataProviderV10,
                ulsterBankDataProviderV10
        );
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnTransportKeyRequirements(GenericBaseDataProvider provider) {
        // when
        KeyRequirements transportKeyRequirements = provider.getTransportKeyRequirements().get();
        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnSigningKeyRequirements(GenericBaseDataProvider provider) {
        // when
        KeyRequirements signingKeyRequirements = provider.getSigningKeyRequirements().get();
        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(RbsGroupAuthMeansBuilderV4.SIGNING_PRIVATE_KEY_ID_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnTypedAuthenticationMeans(GenericBaseDataProvider provider) {
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
