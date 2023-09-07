package com.yolt.providers.openbanking.ais.generic2.signer;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;

public interface PaymentRequestSigner {

    <T> String createRequestSignature(T request, DefaultAuthMeans authMeans, Signer signer);
}
