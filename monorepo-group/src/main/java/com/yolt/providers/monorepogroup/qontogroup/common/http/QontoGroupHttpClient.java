package com.yolt.providers.monorepogroup.qontogroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Organization;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.TokenResponse;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Transactions;
import org.springframework.util.MultiValueMap;

import java.time.Instant;

public interface QontoGroupHttpClient {
    TokenResponse createToken(final MultiValueMap<String, String> tokenRequest) throws TokenInvalidException;

    Organization fetchOrganization(final String accessToken, final String psuIpAddress, final Signer signer, final QontoGroupAuthenticationMeans.SigningData signingData) throws TokenInvalidException;

    Transactions fetchTransactions(final String accessToken,
                                   final String psuIpAddress,
                                   final Signer signer,
                                   final QontoGroupAuthenticationMeans.SigningData signingData,
                                   final String iban,
                                   final Instant dateFrom,
                                   final String pageNumber) throws TokenInvalidException;
}
