package com.yolt.providers.stet.bnpparibasgroup.common.service.fetchdata;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionsResponseDTO;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.transaction.DefaultFetchTransactionsStrategy;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BnpParibasGroupFetchTransactionsStrategy extends DefaultFetchTransactionsStrategy {

    private final Clock clock;

    private static final Period TRANSACTIONS_FETCH_PERIOD = Period.ofDays(89);
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Paris");

    public BnpParibasGroupFetchTransactionsStrategy(FetchDataRestClient restClient, DateTimeSupplier dateTimeSupplier, DefaultProperties properties, Period fetchPeriod, Clock clock) {
        super(restClient, dateTimeSupplier, properties, fetchPeriod);
        this.clock = clock;
    }


    @Override
    public List<StetTransactionDTO> fetchTransactions(HttpClient httpClient, String endpoint, DataRequest dataRequest, Instant fetchStartTime) throws TokenInvalidException {
        ArrayList<StetTransactionDTO> transactions = new ArrayList<>();

        LocalDate transactionsFetchDateFrom = adjustTransactionsFetchDateFrom(fetchStartTime);
        LocalDate dateTo = null;
        LocalDate dateFrom = null;
        int pageLimit = 0;
        do {
            dateTo = adjustDateTo(dateTo, dateFrom);
            dateFrom = adjustDateFrom(dateTo, transactionsFetchDateFrom);

            String url = UriComponentsBuilder.fromUriString(endpoint)
                    .queryParam("dateTo", toDateFormat(dateTo))
                    .queryParam("dateFrom", toDateFormat(dateFrom))
                    .buildAndExpand()
                    .toUriString();

            Optional<StetTransactionsResponseDTO> optionalHalTransactions = Optional.ofNullable(
                    restClient.getTransactions(httpClient, url, dataRequest));

            optionalHalTransactions.ifPresent(halTransactions -> {
                if (Objects.nonNull(halTransactions.getTransactions())) {
                    for (StetTransactionDTO transaction : new ArrayList<>(halTransactions.getTransactions())) {
                        if (!shouldIgnoreTransaction(transaction)) {
                            transactions.add(transaction);
                        }
                    }
                }
            });
            pageLimit++;
        } while (transactionsFetchDateFrom.isBefore(dateFrom) && pageLimit <= properties.getPaginationLimit());

        return transactions;
    }

    private LocalDate adjustTransactionsFetchDateFrom(Instant transactionsFetchStartTime) {
        Instant twelveMonthsAgo = Instant.now(clock).minus(Period.ofDays(365));
        if (transactionsFetchStartTime.isBefore(twelveMonthsAgo)) {
            return dateTimeSupplier.convertToLocalDate(twelveMonthsAgo);
        }
        return dateTimeSupplier.convertToLocalDate(transactionsFetchStartTime);
    }

    private LocalDate adjustDateTo(LocalDate dateTo, LocalDate dateFrom) {
        return dateTo == null ? LocalDate.now(clock) : dateFrom;
    }

    private LocalDate adjustDateFrom(LocalDate dateTo, LocalDate transactionsFetchStartTime) {
        LocalDate dateFrom = dateTo.minus(TRANSACTIONS_FETCH_PERIOD);
        return transactionsFetchStartTime.isBefore(dateFrom) ? dateFrom : transactionsFetchStartTime;
    }

    public static String toDateFormat(LocalDate localDate) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(DEFAULT_ZONE).format(localDate);
    }
}
