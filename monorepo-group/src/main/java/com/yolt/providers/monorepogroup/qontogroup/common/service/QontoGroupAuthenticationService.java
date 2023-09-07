package com.yolt.providers.monorepogroup.qontogroup.common.service;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.internal.QontoGroupProviderState;
import com.yolt.providers.monorepogroup.qontogroup.common.http.QontoGroupHttpClient;

public interface QontoGroupAuthenticationService {

    String getLoginUrl(final QontoGroupAuthenticationMeans authenticationMeans, final String baseRedirectUrl, final String state);

    QontoGroupProviderState createAccessMeans(final QontoGroupAuthenticationMeans authenticationMeans, final QontoGroupHttpClient httpClient, final String baseClientRedirectUrl, final String redirectUrlPostedBackFromSite) throws TokenInvalidException;

    QontoGroupProviderState refreshAccessMeans(final QontoGroupAuthenticationMeans authenticationMeans, final QontoGroupProviderState providerState, final QontoGroupHttpClient httpClient) throws TokenInvalidException;
}
