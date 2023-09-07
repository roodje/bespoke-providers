package com.yolt.providers.monorepogroup.libragroup.common.ais.data;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.dto.Accounts;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.dto.Balances;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.dto.Transactions;

import java.time.Instant;

public interface LibraGroupDataHttpClient {
    Accounts getAccounts(String consentId,
                         String accessToken,
                         LibraGroupAuthenticationMeans.SigningData signingData,
                         Signer signer) throws TokenInvalidException;

    Balances getBalances(String accountId,
                         String consentId,
                         String accessToken) throws TokenInvalidException;

    Transactions getAccountTransactions(String accountId,
                                        String consentId,
                                        String accessToken,
                                        Instant transactionsFetchStartTime) throws TokenInvalidException;
}
