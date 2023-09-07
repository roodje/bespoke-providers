package com.yolt.providers.monorepogroup.cecgroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.cecgroup.common.CecGroupAccessMeans;
import com.yolt.providers.monorepogroup.cecgroup.common.auth.CecGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.consent.ConsentCreationResponse;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.data.AccountsResponse;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.data.TransactionsResponse;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.token.TokenResponse;

import java.time.LocalDate;

public interface CecGroupHttpClient {

    ConsentCreationResponse createConsent(CecGroupAuthenticationMeans authMeans,
                                          Signer signer,
                                          LocalDate consentTo,
                                          String psuIpAddress,
                                          String redirectUri,
                                          String state) throws TokenInvalidException;

    TokenResponse createToken(String clientId, String clientSecret, String redirectUri, String authCode) throws TokenInvalidException;

    TokenResponse refreshToken(String clientId, String refreshToken) throws TokenInvalidException;

    AccountsResponse fetchAccounts(CecGroupAuthenticationMeans authenticationMeans,
                                   CecGroupAccessMeans cecGroupAccessMeans,
                                   Signer signer,
                                   String psuIpAddress);

    TransactionsResponse fetchFirstPageOfTransactions(CecGroupAuthenticationMeans authenticationMeans,
                                                      CecGroupAccessMeans cecGroupAccessMeans,
                                                      Signer signer,
                                                      String psuIpAddress,
                                                      String accountId,
                                                      String dateFrom);

    TransactionsResponse fetchNextPageOfTransactions(String nextPageUrl,
                                                     CecGroupAuthenticationMeans authenticationMeans,
                                                     CecGroupAccessMeans cecGroupAccessMeans,
                                                     Signer signer,
                                                     String psuIpAddress);
}
