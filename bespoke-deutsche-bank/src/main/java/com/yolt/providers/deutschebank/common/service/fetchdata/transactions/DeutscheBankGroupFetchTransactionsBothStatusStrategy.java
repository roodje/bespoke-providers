package com.yolt.providers.deutschebank.common.service.fetchdata.transactions;

import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.deutschebank.common.config.DeutscheBankGroupDateConverter;
import com.yolt.providers.deutschebank.common.config.DeutscheBankGroupProperties;
import com.yolt.providers.deutschebank.common.domain.DeutscheBankGroupProviderState;
import com.yolt.providers.deutschebank.common.domain.model.fetchdata.TransactionsResponse;
import com.yolt.providers.deutschebank.common.http.DeutscheBankGroupHttpClient;
import com.yolt.providers.deutschebank.common.mapper.DeutscheBankGroupTransactionMapper;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;

@RequiredArgsConstructor
public class DeutscheBankGroupFetchTransactionsBothStatusStrategy implements DeutscheBankGroupFetchTransactionsStrategy {

    private static final String TRANSACTIONS_TEMPLATE = "/v1/accounts/{accountId}/transactions";

    private final DeutscheBankGroupTransactionMapper transactionMapper;
    private final DeutscheBankGroupProperties properties;
    private final DeutscheBankGroupDateConverter dateConverter;

    @Override
    public List<ProviderTransactionDTO> fetchTransactions(DeutscheBankGroupHttpClient httpClient,
                                                          UrlFetchDataRequest request,
                                                          DeutscheBankGroupProviderState providerState,
                                                          String accountId,
                                                          String accountName) throws TokenInvalidException {
        List<ProviderTransactionDTO> transactionsDTO = new ArrayList<>();

        String url = UriComponentsBuilder.fromUriString(TRANSACTIONS_TEMPLATE)
                .queryParam("bookingStatus", "both")
                .queryParam("dateFrom", dateConverter.toNarrowedDateFormat(request.getTransactionsFetchStartTime(), 90))
                .buildAndExpand(accountId)
                .toUriString();

        int pageCounter = 1;
        while (StringUtils.isNotEmpty(url) && (pageCounter <= properties.getPaginationLimit())) {
            TransactionsResponse response = httpClient.getTransactions(url, providerState, request.getPsuIpAddress());
            transactionsDTO.addAll(transactionMapper.mapProviderTransactionsDTO(response.getPendingTransactions(), PENDING, response.getAccount().getIban(), accountName));
            transactionsDTO.addAll(transactionMapper.mapProviderTransactionsDTO(response.getBookedTransactions(), BOOKED, response.getAccount().getIban(), accountName));

            url = response.getNextHref();
            pageCounter++;
        }
        return transactionsDTO;
    }
}
