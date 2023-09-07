package com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;

public interface AccountRequestService {

    String requestNewAccountRequestId(final HttpClient httpClient,
                                      final DefaultAuthMeans authenticationMeans,
                                      final AuthenticationMeansReference authenticationMeansReference,
                                      final TokenScope scope,
                                      final Signer signer) throws TokenInvalidException;

    void deleteAccountRequest(final HttpClient httpClient,
                              final DefaultAuthMeans authenticationMeans,
                              final AuthenticationMeansReference authenticationMeansReference,
                              final String externalConsentId,
                              final TokenScope scope,
                              final Signer signer) throws TokenInvalidException;
}
