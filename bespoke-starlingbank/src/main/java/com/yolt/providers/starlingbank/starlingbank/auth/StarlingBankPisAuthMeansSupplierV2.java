package com.yolt.providers.starlingbank.starlingbank.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAbstractAuthMeansSupplier;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthMeansSupplier;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans.*;

@Service
public class StarlingBankPisAuthMeansSupplierV2 extends StarlingBankAbstractAuthMeansSupplier implements StarlingBankAuthMeansSupplier {

    @Override
    public StarlingBankAuthenticationMeans createAuthenticationMeans(Map<String, BasicAuthenticationMean> basicAuthMeans,
                                                                     String providerIdentifier) {
        return StarlingBankAuthenticationMeans.builder()
                .apiKey(getValue(basicAuthMeans, providerIdentifier, API_KEY_NAME_2))
                .apiSecret(getValue(basicAuthMeans, providerIdentifier, API_SECRET_NAME_2))
                .transportCertificate(createTransportCertificate(providerIdentifier, basicAuthMeans, TRANSPORT_CERTIFICATE_NAME_2))
                .transportKeyId(getNullableValue(basicAuthMeans, TRANSPORT_KEY_ID_NAME_2))
                .signingKeyHeaderId(getNullableValue(basicAuthMeans, SIGNING_KEY_HEADER_ID_NAME_2))
                .signingPrivateKeyId(getNullableValue(basicAuthMeans, SIGNING_PRIVATE_KEY_ID_NAME_2))
                .build();
    }
}
