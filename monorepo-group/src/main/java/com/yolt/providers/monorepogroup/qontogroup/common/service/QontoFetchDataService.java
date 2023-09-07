package com.yolt.providers.monorepogroup.qontogroup.common.service;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.internal.QontoFetchDataResult;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.internal.QontoGroupProviderState;
import com.yolt.providers.monorepogroup.qontogroup.common.http.QontoGroupHttpClient;

import java.time.Instant;

public interface QontoFetchDataService {
    QontoFetchDataResult fetchAccount(final QontoGroupAuthenticationMeans authenticationMeans, final QontoGroupHttpClient httpClient, final QontoGroupProviderState providerState, final Instant transactionsFetchStartTime, final String psuIpAddress, final Signer signer) throws TokenInvalidException;
}
