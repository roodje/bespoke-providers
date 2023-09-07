package com.yolt.providers.yoltprovider.pis;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.types.UuidType;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.UUID;

@UtilityClass
public class TestPaymentAuthMeansUtil {

    public Map<String, BasicAuthenticationMean> getBasicAuthMeans(UUID clientId, UUID publicKid, UUID signingKid) {
        return Map.of(
                PaymentAuthenticationMeans.CLIENT_ID, new BasicAuthenticationMean(UuidType.getInstance(), clientId.toString()),
                PaymentAuthenticationMeans.PUBLIC_KID, new BasicAuthenticationMean(UuidType.getInstance(), publicKid.toString()),
                PaymentAuthenticationMeans.PRIVATE_KID, new BasicAuthenticationMean(UuidType.getInstance(), signingKid.toString())
        );
    }
}
