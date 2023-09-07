package com.yolt.providers.cbiglobe.common.service;

import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import com.yolt.providers.cbiglobe.common.mapper.CbiGlobeCardAccountMapper;
import com.yolt.providers.cbiglobe.common.model.CbiGlobeAccessMeansDTO;
import com.yolt.providers.cbiglobe.common.model.SignatureData;
import com.yolt.providers.cbiglobe.common.util.CbiGlobeErrorHandlerUtil;
import com.yolt.providers.cbiglobe.common.util.CbiGlobeHttpHeaderUtil;
import com.yolt.providers.cbiglobe.dto.*;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.yolt.providers.cbiglobe.common.util.CbiGlobeDateUtil.toDateFormat;
import static com.yolt.providers.cbiglobe.common.util.CbiGlobeHttpHeaderUtil.CPAAS_TOTAL_PAGES_HEADER;
import static org.springframework.http.HttpMethod.GET;

@RequiredArgsConstructor
public class CbiGlobeFetchCardDataService implements CbiGlobeFetchDataService {

    private static final String GET_CARD_ACCOUNTS_ENDPOINT_TEMPLATE = "/2.3.2/card-accounts";
    private static final String GET_CARD_BALANCES_ENDPOINT_TEMPLATE = "/2.3.2/card-accounts/{account-id}/balances";
    private static final String GET_CARD_TRANSACTIONS_ENDPOINT_TEMPLATE = "/2.3.2/card-accounts/{id}/transactions";

    private final CbiGlobeBaseProperties properties;
    private final CbiGlobeCardAccountMapper accountMapper;
    private final Clock clock;

    @Override
    public List<ProviderAccountDTO> fetchAccounts(RestTemplate restTemplate,
                                                  CbiGlobeAccessMeansDTO accessMeans,
                                                  SignatureData signatureData,
                                                  String aspspCode,
                                                  String psuIpAddress) {
        HttpHeaders headers = CbiGlobeHttpHeaderUtil.getFetchDataHeaders(
                accessMeans.getAccessToken(), aspspCode, accessMeans.getConsentId(), signatureData, psuIpAddress, clock);

        ResponseEntity<ReadCardAccountListResponseType> response;
        response = restTemplate.exchange(GET_CARD_ACCOUNTS_ENDPOINT_TEMPLATE,
                GET,
                new HttpEntity<>(headers),
                ReadCardAccountListResponseType.class);

        ReadCardAccountListResponseType accountsResponse = response.getBody();
        if (Objects.isNull(accountsResponse)) {
            throw new IllegalStateException("Received empty body fetching accounts");
        }

        return accountsResponse.getCardAccounts().stream()
                .map(accountMapper::mapToProviderAccountDTO)
                .toList();
    }

    @Override
    public DataProviderResponse fetchTransactionsForAccounts(RestTemplate restTemplate,
                                                             CbiGlobeAccessMeansDTO accessMeans,
                                                             Instant transactionsFetchStartTime,
                                                             SignatureData signatureData,
                                                             String aspspCode,
                                                             String psuIpAddress) throws ProviderFetchDataException, TokenInvalidException {
        List<ProviderAccountDTO> providerAccountDTOs = new ArrayList<>();
        for (Map.Entry<String, ProviderAccountDTO> consentedAccount : accessMeans.getConsentedAccounts().entrySet()) {
            String consentId = consentedAccount.getKey();
            ProviderAccountDTO account = consentedAccount.getValue();
            try {
                List<ReadCardAccountTransactionListResponseTypeTransactions> transactions = getAllTransactions(
                        restTemplate, accessMeans.getAccessToken(), consentId, aspspCode, signatureData, account.getAccountId(), transactionsFetchStartTime, psuIpAddress);

                List<ReadCardAccountBalancesResponseTypeBalances> balances = getBalances(
                        restTemplate, accessMeans.getAccessToken(), consentId, aspspCode, signatureData, account.getAccountId(), psuIpAddress);

                ProviderAccountDTO providerAccountDTO = accountMapper
                        .updateProviderAccountDTO(account, balances, transactions);

                providerAccountDTOs.add(providerAccountDTO);
            } catch (BackPressureRequestException e) {
                throw e;
            } catch (Exception e) {
                throw new ProviderFetchDataException(e);
            }
        }
        return new DataProviderResponse(providerAccountDTOs);
    }

    private List<ReadCardAccountBalancesResponseTypeBalances> getBalances(RestTemplate restTemplate,
                                                                          String accessToken,
                                                                          String consentId,
                                                                          String aspspCode,
                                                                          SignatureData signatureData,
                                                                          String resourceId,
                                                                          String psuIpAddress) throws TokenInvalidException, ProviderFetchDataException {
        HttpHeaders headers = CbiGlobeHttpHeaderUtil.getFetchDataHeaders(
                accessToken, aspspCode, consentId, signatureData, psuIpAddress, clock);

        HttpEntity httpEntity = new HttpEntity(headers);
        try {
            return Objects.requireNonNull(restTemplate
                            .exchange(GET_CARD_BALANCES_ENDPOINT_TEMPLATE, GET, httpEntity, ReadCardAccountBalancesResponseType.class, resourceId)
                            .getBody())
                    .getBalances();
        } catch (HttpStatusCodeException e) {
            CbiGlobeErrorHandlerUtil.handleNon2xxResponseCodeFetchData(e.getStatusCode(), psuIpAddress);
            throw e;
        }
    }

    private List<ReadCardAccountTransactionListResponseTypeTransactions> getAllTransactions(RestTemplate restTemplate,
                                                                                            String accessToken,
                                                                                            String consentId,
                                                                                            String aspspCode,
                                                                                            SignatureData signatureData,
                                                                                            String resourceId,
                                                                                            Instant transactionsFetchStartTime,
                                                                                            String psuIpAddress) throws TokenInvalidException, ProviderFetchDataException {
        HttpHeaders headers = CbiGlobeHttpHeaderUtil.getTransactionsHeaders(
                accessToken, aspspCode, consentId, signatureData, psuIpAddress, clock);

        List<ReadCardAccountTransactionListResponseTypeTransactions> transactions = new ArrayList<>();
        ResponseEntity<ReadCardAccountTransactionListResponseType> response;
        int nextPageOffset = 1;
        do {
            String url = UriComponentsBuilder.fromUriString(GET_CARD_TRANSACTIONS_ENDPOINT_TEMPLATE)
                    .queryParam("booking_status", "booked")
                    .queryParam("date_from", toDateFormat(transactionsFetchStartTime))
                    .queryParam("date_to", toDateFormat(Instant.now(clock)))
                    .queryParam("offset", nextPageOffset)
                    .queryParam("limit", properties.getTransactionsPaginationLimit())
                    .buildAndExpand(resourceId)
                    .toUriString();

            HttpEntity httpEntity = new HttpEntity(headers);
            try {
                response = restTemplate.exchange(url, GET, httpEntity, ReadCardAccountTransactionListResponseType.class);
            } catch (HttpStatusCodeException e) {
                CbiGlobeErrorHandlerUtil.handleNon2xxResponseCodeFetchData(e.getStatusCode(), psuIpAddress);
                throw e;
            }
            ReadCardAccountTransactionListResponseType transactionsBody = response.getBody();
            if (transactionsBody != null) {
                transactions.add(transactionsBody.getTransactions());
            }
            nextPageOffset++;
        } while (shouldContinueFetchingAdditionalPages(response.getHeaders(), nextPageOffset));
        return transactions;
    }

    private boolean shouldContinueFetchingAdditionalPages(HttpHeaders headers, int currentOffset) {
        List<String> totalPagesHeaders = headers.get(CPAAS_TOTAL_PAGES_HEADER);
        if (CollectionUtils.isEmpty(totalPagesHeaders)) {
            return false;
        }
        String totalPagesHeader = totalPagesHeaders.get(0);
        return StringUtils.isNotEmpty(totalPagesHeader) && currentOffset <= Integer.parseInt(totalPagesHeader);
    }
}
