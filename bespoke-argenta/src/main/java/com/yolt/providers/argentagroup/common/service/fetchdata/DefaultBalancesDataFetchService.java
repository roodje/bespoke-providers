package com.yolt.providers.argentagroup.common.service.fetchdata;

import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.argentagroup.common.service.token.AccessMeans;
import com.yolt.providers.argentagroup.dto.GetBalancesResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RequiredArgsConstructor
public class DefaultBalancesDataFetchService implements BalancesDataFetchService {

    private static final String ACCOUNT_ID_PARAMETER_NAME = "accountId";

    private final String accountBalancesEndpointPath;
    private final FetchDataHttpHeadersProvider fetchDataHttpHeadersProvider;
    private final HttpErrorHandler balancesHttpErrorHandler;
    private final BalancesMapper balancesMapper;

    @Override
    public Map<BalanceType, BalanceDTO> getBalances(final UrlFetchDataRequest request,
                                                    final DefaultAuthenticationMeans authenticationMeans,
                                                    final HttpClient httpClient,
                                                    final ProviderAccountDTO account,
                                                    final AccessMeans accessMeans) throws TokenInvalidException {
        HttpHeaders requestHeaders = fetchDataHttpHeadersProvider.providerHeaders(request, authenticationMeans, accessMeans);

        String balancesUri = String.format(accountBalancesEndpointPath, account.getAccountId());
        String uri = UriComponentsBuilder.fromPath(accountBalancesEndpointPath)
                .uriVariables(Map.of(ACCOUNT_ID_PARAMETER_NAME, account.getAccountId()))
                .toUriString();

        GetBalancesResponse balancesResponse = httpClient.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(requestHeaders),
                ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID,
                GetBalancesResponse.class,
                balancesHttpErrorHandler
        ).getBody();

        return balancesMapper.mapBalances(balancesResponse);
    }
}
