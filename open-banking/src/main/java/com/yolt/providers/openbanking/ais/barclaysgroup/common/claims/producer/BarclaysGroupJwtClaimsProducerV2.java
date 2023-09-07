package com.yolt.providers.openbanking.ais.barclaysgroup.common.claims.producer;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import org.jose4j.jwt.JwtClaims;

import java.util.Arrays;
import java.util.function.Function;

public class BarclaysGroupJwtClaimsProducerV2 extends DefaultJwtClaimsProducer {

    private static final String ACR_VALUES = "acr_values";
    private static final String BARCLAYS_OAUTH_URL_REDUNTANT_ENDING = "/as";

    public BarclaysGroupJwtClaimsProducerV2(Function<DefaultAuthMeans, String> issuerSupplier, String audience) {
        super(issuerSupplier, audience);
    }

    @Override
    public JwtClaims createJwtClaims(DefaultAuthMeans authenticationMeans,
                                     String secretState,
                                     String redirectUrl,
                                     TokenScope scope,
                                     String... args) {
        JwtClaims claims = super.createJwtClaims(authenticationMeans, secretState, redirectUrl, scope, args);
        claims.setClaim(ACR_VALUES, "urn:openbanking:psd2:sca urn:openbanking:psd2:ca");

        String audience = Arrays.stream(args)
                .findAny()
                .map(arg -> arg.substring(0, arg.indexOf(BARCLAYS_OAUTH_URL_REDUNTANT_ENDING)))
                .orElseThrow(() -> new IllegalArgumentException("OAuth ulr is null."));
        claims.setAudience(audience);

        return claims;
    }
}
