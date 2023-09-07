package com.yolt.providers.monorepogroup.olbgroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.model.consent.ConsentCreationRequest;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.model.consent.ConsentCreationResponse;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.model.fetchdata.AccountsResponse;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.model.fetchdata.BalancesResponse;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.model.fetchdata.TransactionsResponse;

public interface OlbGroupHttpClient {

    ConsentCreationResponse createConsent(ConsentCreationRequest request,
                                          String psuIpAddress,
                                          String psuId,
                                          String redirectUri) throws TokenInvalidException;

    void deleteConsent(String consentId) throws TokenInvalidException;

    AccountsResponse getAccounts(String consentId, String psuIpAddress) throws TokenInvalidException;

    BalancesResponse getBalances(String accountId, String consentId, String psuIpAddress) throws TokenInvalidException;

    TransactionsResponse getTransactions(String url, String consentId, String psuIpAddress) throws TokenInvalidException;

    TransactionsResponse getTransactions(String accountId, String consentId, String psuIpAddress, String dateFrom) throws TokenInvalidException;
}
