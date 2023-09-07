package com.yolt.providers.deutschebank.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.deutschebank.common.domain.DeutscheBankGroupProviderState;
import com.yolt.providers.deutschebank.common.domain.model.consent.ConsentCreationRequest;
import com.yolt.providers.deutschebank.common.domain.model.consent.ConsentCreationResponse;
import com.yolt.providers.deutschebank.common.domain.model.consent.ConsentStatusResponse;
import com.yolt.providers.deutschebank.common.domain.model.fetchdata.AccountsResponse;
import com.yolt.providers.deutschebank.common.domain.model.fetchdata.BalancesResponse;
import com.yolt.providers.deutschebank.common.domain.model.fetchdata.TransactionsResponse;

public interface DeutscheBankGroupHttpClient {

    ConsentCreationResponse postConsentCreation(String url, ConsentCreationRequest request, String psuIpAddress, String psuId, String redirectUri, String nokRedirectUri) throws TokenInvalidException;

    ConsentStatusResponse getConsentStatus(String url, String psuIpAddress) throws TokenInvalidException;

    AccountsResponse getAccounts(String url, DeutscheBankGroupProviderState providerState, String psuIpAddress) throws TokenInvalidException;

    BalancesResponse getBalances(String url, DeutscheBankGroupProviderState providerState, String psuIpAddress) throws TokenInvalidException;

    TransactionsResponse getTransactions(String url, DeutscheBankGroupProviderState providerState, String psuIpAddress) throws TokenInvalidException;
}
