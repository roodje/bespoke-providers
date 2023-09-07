package com.yolt.providers.monorepogroup.atruviagroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.external.*;

import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.UUID;

public interface AtruviaGroupHttpClient {

    StartScaProcessResponse createAuthorisationForConsent(String consentId, String psuIdName, String psuIdPassword,
                                                          X509Certificate signingCertificate,
                                                          UUID signingKeyId,
                                                          Signer signer);

    SelectPsuAuthenticationMethodResponse selectSCAForConsentAndAuthorisation(String consentId, String authorisationId, String authenticationid,
                                                                              X509Certificate signingCertificate,
                                                                              UUID signingKeyId,
                                                                              Signer signer);

    ScaStatusResponse putAuthenticationData(String consentId, String authorisationId, String authenticationData,
                                            X509Certificate signingCertificate,
                                            UUID signingKeyId,
                                            Signer signer);

    AccountsResponse getAccounts(String consentId,
                                 String psuIpAddress,
                                 X509Certificate signingCertificate,
                                 UUID signingKeyId,
                                 Signer signer) throws TokenInvalidException;

    BalancesResponse getBalances(String accountId,
                                 String consentId,
                                 String psuIpAddress,
                                 X509Certificate signingCertificate,
                                 UUID signingKeyId,
                                 Signer signer) throws TokenInvalidException;

    TransactionsResponse getTransactions(String url,
                                         String consentId,
                                         String psuIpAddress,
                                         X509Certificate signingCertificate,
                                         UUID signingKeyId,
                                         Signer signer) throws TokenInvalidException;

    TransactionsResponse getTransactions(String accountId,
                                         String consentId,
                                         String psuIpAddress,
                                         String dateFrom,
                                         X509Certificate signingCertificate,
                                         UUID signingKeyId,
                                         Signer signer) throws TokenInvalidException;


    ConsentsResponse201 createConsentForAllAccounts(LocalDate validity,
                                                    String psuIdName,
                                                    String psuIpAddress,
                                                    X509Certificate signingCertificate,
                                                    UUID signingKeyId,
                                                    Signer signer);
}
