package com.yolt.providers.monorepogroup.chebancagroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.chebancagroup.common.auth.CheBancaGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.external.*;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.time.Instant;

public interface CheBancaGroupHttpClient {

    CheBancaGroupToken createClientCredentialToken(final Signer signer, final MultiValueMap<String, String> requestBody, final CheBancaGroupAuthenticationMeans authenticationMeans) throws TokenInvalidException;

    CheBancaGroupToken createRefreshToken(final Signer signer, final MultiValueMap<String, String> requestBody, final CheBancaGroupAuthenticationMeans authenticationMeans) throws TokenInvalidException;

    ResponseEntity<String> createAuthorizationSession(final Signer signer, final String authorizationUrl, final CheBancaGroupAuthenticationMeans authenticationMeans) throws TokenInvalidException;

    AccountResponse fetchAccounts(final Signer signer, final CheBancaGroupAuthenticationMeans authenticationMeans, final String clientAccessToken, final String customerId) throws TokenInvalidException;

    CustomerIdResponse getCustomerId(final Signer signer, final CheBancaGroupAuthenticationMeans authenticationMeans, final String clientAccessToken) throws TokenInvalidException;

    TransactionResponse fetchTransactions(final Signer signer, final CheBancaGroupAuthenticationMeans authenticationMeans, final String clientAccessToken, final String nextHrefUrl) throws TokenInvalidException;

    TransactionResponse fetchTransactions(final Signer signer, final CheBancaGroupAuthenticationMeans authenticationMeans, final String clientAccessToken, final String customerId, final String productId, final Instant transactionsFetchStartTime) throws TokenInvalidException;

    Balances fetchBalances(final Signer signer, final CheBancaGroupAuthenticationMeans authenticationMeans, final String clientAccessToken, final String customerId, final String productId) throws TokenInvalidException;

}
