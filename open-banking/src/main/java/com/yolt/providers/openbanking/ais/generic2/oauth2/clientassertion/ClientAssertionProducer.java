package com.yolt.providers.openbanking.ais.generic2.oauth2.clientassertion;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;

public interface ClientAssertionProducer {
    String createNewClientRequestToken(DefaultAuthMeans authenticationMeans,
                                       Signer signer) throws TokenInvalidException;
}
