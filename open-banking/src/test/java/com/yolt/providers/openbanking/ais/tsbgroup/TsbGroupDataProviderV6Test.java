package com.yolt.providers.openbanking.ais.tsbgroup;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.tsbgroup.common.TsbGroupBaseDataProvider;
import com.yolt.providers.openbanking.ais.tsbgroup.common.auth.TsbGroupAuthMeansBuilderV3;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TsbGroupDataProviderV6Test {

    private TsbGroupBaseDataProvider tsbDataProviderV6 = new TsbGroupBaseDataProvider(null,
            null,
            null,
            null,
            null,
            null,
            typedAuthMeans -> TsbGroupAuthMeansBuilderV3.createAuthenticationMeans(typedAuthMeans, "TSB_BANK"),
            TsbGroupAuthMeansBuilderV3.getTypedAuthenticationMeans(),
            null,
            () -> HsmUtils.getKeyRequirements(TsbGroupAuthMeansBuilderV3.SIGNING_PRIVATE_KEY_ID_NAME),
            () -> HsmUtils.getKeyRequirements(TsbGroupAuthMeansBuilderV3.TRANSPORT_PRIVATE_KEY_ID_NAME, TsbGroupAuthMeansBuilderV3.TRANSPORT_CERTIFICATE_NAME),
            null,
            null);

    private Stream<UrlDataProvider> getTtbProvidersWithAuthMeansV2() {
        return Stream.of(tsbDataProviderV6);
    }

    @ParameterizedTest
    @MethodSource("getTtbProvidersWithAuthMeansV2")
    public void shouldReturnTransportKeyRequirements(UrlDataProvider provider) {
        // when
        KeyRequirements transportKeyRequirements = provider.getTransportKeyRequirements().get();
        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(TsbGroupAuthMeansBuilderV3.TRANSPORT_PRIVATE_KEY_ID_NAME, TsbGroupAuthMeansBuilderV3.TRANSPORT_CERTIFICATE_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getTtbProvidersWithAuthMeansV2")
    public void shouldReturnSigningKeyRequirements(UrlDataProvider provider) {
        // when
        KeyRequirements signingKeyRequirements = provider.getSigningKeyRequirements().get();
        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(TsbGroupAuthMeansBuilderV3.SIGNING_PRIVATE_KEY_ID_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getTtbProvidersWithAuthMeansV2")
    public void shouldReturnTypedAuthenticationMeansV2Only(UrlDataProvider provider) {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = provider.getTypedAuthenticationMeans();
        // then
        assertThat(authenticationMeans).containsOnlyKeys(
                TsbGroupAuthMeansBuilderV3.INSTITUTION_ID_NAME,
                TsbGroupAuthMeansBuilderV3.CLIENT_ID_NAME,
                TsbGroupAuthMeansBuilderV3.SIGNING_KEY_HEADER_ID_NAME,
                TsbGroupAuthMeansBuilderV3.SIGNING_PRIVATE_KEY_ID_NAME,
                TsbGroupAuthMeansBuilderV3.TRANSPORT_CERTIFICATE_NAME,
                TsbGroupAuthMeansBuilderV3.TRANSPORT_PRIVATE_KEY_ID_NAME,
                TsbGroupAuthMeansBuilderV3.ORGANIZATION_ID_NAME,
                TsbGroupAuthMeansBuilderV3.CLIENT_SECRET_NAME,
                TsbGroupAuthMeansBuilderV3.SOFTWARE_ID_NAME,
                TsbGroupAuthMeansBuilderV3.SOFTWARE_STATEMENT_ASSERTION_NAME
        );
    }
}
