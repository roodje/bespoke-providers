package com.yolt.providers.monorepogroup.qontogroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV3;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Organization;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.TokenResponse;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Transactions;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.QontoGroupDateMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

public class DefaultQontoGroupHttpClient extends DefaultHttpClientV3 implements QontoGroupHttpClient {

    private static final String ORGANIZATION_URL = "/v2/organization";

    private static final String TRANSACTION_URL = "/v2/transactions?iban={iban}&emitted_at_from={dateFrom}&current_page={pageNumber}";
    private final QontoGroupDateMapper dateMapper;
    private final HttpErrorHandlerV2 httpErrorHandler;

    private final String tokenUrl;

    public DefaultQontoGroupHttpClient(final MeterRegistry registry, final RestTemplate restTemplate, final String provider, final HttpErrorHandlerV2 errorHandler, final String tokenUrl, final QontoGroupDateMapper dateMapper) {
        super(registry, restTemplate, provider);
        this.httpErrorHandler = errorHandler;
        this.tokenUrl = tokenUrl;
        this.dateMapper = dateMapper;
    }

    @Override
    public TokenResponse createToken(final MultiValueMap<String, String> tokenRequest) throws TokenInvalidException {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return exchange(tokenUrl, HttpMethod.POST, new HttpEntity<>(tokenRequest, headers), ProviderClientEndpoints.GET_ACCESS_TOKEN, TokenResponse.class, httpErrorHandler).getBody();
    }

    @Override
    public Organization fetchOrganization(final String accessToken,
                                          final String psuIpAddress,
                                          final Signer signer,
                                          final QontoGroupAuthenticationMeans.SigningData signingData) throws TokenInvalidException {
        QontoGroupHttpHeaders headers = getFetchDataHeaders(accessToken, psuIpAddress, signer, signingData);
        return exchange(ORGANIZATION_URL, HttpMethod.GET, new HttpEntity<>(headers), ProviderClientEndpoints.GET_ACCOUNTS, Organization.class, httpErrorHandler).getBody();
    }

    @Override
    public Transactions fetchTransactions(final String accessToken,
                                          final String psuIpAddress,
                                          final Signer signer,
                                          final QontoGroupAuthenticationMeans.SigningData signingData,
                                          final String iban,
                                          final Instant dateFrom,
                                          final String pageNumber) throws TokenInvalidException {
        var headers = getFetchDataHeaders(accessToken, psuIpAddress, signer, signingData);
        var formattedDateFrom = dateMapper.mapHttpRequestDateFormat(dateFrom);
        return exchange(TRANSACTION_URL, HttpMethod.GET, new HttpEntity<>(headers), ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID, Transactions.class, httpErrorHandler, iban, formattedDateFrom, pageNumber).getBody();
    }

    private QontoGroupHttpHeaders getFetchDataHeaders(final String accessToken, final String psuIpAddress, final Signer signer, final QontoGroupAuthenticationMeans.SigningData signingData) {
        var headers = new QontoGroupHttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.isNotEmpty(psuIpAddress)) {
            headers.add("PSU-IP-Address", psuIpAddress);
        }
        headers.sign(signingData, signer);
        headers.setBearerAuth(accessToken);
        return headers;
    }
}
