package com.yolt.providers.openbanking.ais.generic2.service.restclient;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;

public interface RestClient {

    <T> T fetchAccounts(final HttpClient httpClient,
                        final String currentPath,
                        final AccessMeans accessToken,
                        final String institutionId,
                        final Class<T> responseType) throws TokenInvalidException;

    <T> T fetchTransactions(final HttpClient httpClient,
                            final String currentPath,
                            final AccessMeans accessToken,
                            final String institutionId,
                            final Class<T> responseType) throws TokenInvalidException;

    <T> T fetchDirectDebits(final HttpClient httpClient,
                            final String currentPath,
                            final AccessMeans accessToken,
                            final String institutionId,
                            final Class<T> responseType) throws TokenInvalidException;

    <T> T fetchStandingOrders(final HttpClient httpClient,
                              final String currentPath,
                              final AccessMeans accessToken,
                              final String institutionId,
                              final Class<T> responseType) throws TokenInvalidException;

    <T> T fetchBalances(final HttpClient httpClient,
                        final String currentPath,
                        final AccessMeans accessToken,
                        final String institutionId,
                        final Class<T> responseType) throws TokenInvalidException;

    <T> T createPayment(final HttpClient httpClient,
                        final String exchangePath,
                        final AccessMeans clientAccessToken,
                        final DefaultAuthMeans authMeans,
                        final Object requestBody,
                        final Class<T> responseType,
                        final Signer signer) throws TokenInvalidException;

    <T> T submitPayment(final HttpClient httpClient,
                        final String exchangePath,
                        final AccessMeans userAccessToken,
                        final DefaultAuthMeans authMeans,
                        final Object requestBody,
                        final Class<T> responseType,
                        final Signer signer) throws TokenInvalidException;

    <T> T postAccountAccessConsents(final HttpClient httpClient,
                                    final String exchangePath,
                                    final AccessMeans clientAccessToken,
                                    final DefaultAuthMeans authMeans,
                                    final Object body,
                                    final Class<T> responseType) throws TokenInvalidException;

    <T> T getAccountAccessConsent(final HttpClient httpClient,
                                  final String exchangePath,
                                  final AccessMeans clientAccessToken,
                                  final DefaultAuthMeans authMeans,
                                  final Class<T> responseType) throws TokenInvalidException;

    void deleteAccountAccessConsent(final HttpClient httpClient,
                                    final String exchangePath,
                                    final AccessMeans clientAccessToken,
                                    final String consentId,
                                    final DefaultAuthMeans authMeans) throws TokenInvalidException;
}
