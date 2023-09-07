package com.yolt.providers.brdgroup.common.http;

import com.yolt.providers.brdgroup.common.BrdGroupAccessMeans;
import com.yolt.providers.brdgroup.common.dto.consent.CreateConsentRequest;
import com.yolt.providers.brdgroup.common.dto.consent.CreateConsentResponse;
import com.yolt.providers.brdgroup.common.dto.consent.GetConsentResponse;
import com.yolt.providers.brdgroup.common.dto.fetchdata.AccountsResponse;
import com.yolt.providers.brdgroup.common.dto.fetchdata.BalancesResponse;
import com.yolt.providers.brdgroup.common.dto.fetchdata.TransactionsResponse;

public interface BrdGroupHttpClient {

    CreateConsentResponse postConsentCreation(CreateConsentRequest request, String psuIpAddress, String psuId);

    GetConsentResponse getConsentStatus(String consentId);

    void deleteConsent(String consentId);

    AccountsResponse getAccounts(BrdGroupAccessMeans accessMeans, String psuIpAddress);

    BalancesResponse getBalances(BrdGroupAccessMeans accessMeans, String psuIpAddress, String accountId);

    TransactionsResponse getTransactions(BrdGroupAccessMeans accessMeans, String accountId, String transactionFetchStart, String psuIpAddress);

    TransactionsResponse getTransactionsNextPage(String nextPageEndpoint, BrdGroupAccessMeans accessMeans, String psuIpAddress);

}
