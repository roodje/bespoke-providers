package com.yolt.providers.starlingbank.common.paymentexecutioncontext;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpHeadersProducer;
import com.yolt.providers.starlingbank.common.http.signer.StarlingBankHttpCavageSigner;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.UUID;

import static com.yolt.securityutils.signing.SignatureAlgorithm.SHA256_WITH_RSA;

@RequiredArgsConstructor
public class StarlingBankHttpHeadersProducerFactory {

    private final Clock clock;

    public StarlingBankHttpHeadersProducer createHeadersProducer(StarlingBankAuthenticationMeans authMeans, Signer signer) {
        UUID signingPrivateKeyId = authMeans.getSigningPrivateKeyId() == null ? null : UUID.fromString(authMeans.getSigningPrivateKeyId());
        StarlingBankHttpCavageSigner cavageSigner = new StarlingBankHttpCavageSigner(signer, signingPrivateKeyId, SHA256_WITH_RSA);
        return new StarlingBankHttpHeadersProducer(cavageSigner, clock);
    }
}
