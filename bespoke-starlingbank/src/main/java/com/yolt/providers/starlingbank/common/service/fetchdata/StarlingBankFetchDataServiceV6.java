package com.yolt.providers.starlingbank.common.service.fetchdata;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpClient;
import com.yolt.providers.starlingbank.common.mapper.StarlingBankDataMapperV3;
import com.yolt.providers.starlingbank.common.mapper.StarlingBankTokenMapper;
import com.yolt.providers.starlingbank.common.model.*;
import com.yolt.providers.starlingbank.common.model.domain.Token;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class StarlingBankFetchDataServiceV6 implements StarlingBankFetchDataService {

    private static final String GET_ACCOUNTS_URL = "/api/v2/accounts";
    private static final String GET_ACCOUNTS_IDENTIFIERS_TEMPLATE = "/api/v2/accounts/%s/identifiers";
    private static final String GET_BALANCE_TEMPLATE = "/api/v2/accounts/%s/balance";
    private static final String GET_TRANSACTIONS_TEMPLATE = "/api/v2/feed/account/%s/category/%s/transactions-between";
    private static final String GET_HOLDER_NAME_URL = "/api/v2/account-holder/name";
    private final StarlingBankTokenMapper tokenMapper;
    private final Clock clock;

    public DataProviderResponse getAccountsAndTransactions(StarlingBankHttpClient httpClient, AccessMeansDTO accessMeansDTO, Instant fetchStartTime) throws ProviderFetchDataException, TokenInvalidException {
        Token oAuthToken = tokenMapper.mapToToken(accessMeansDTO);
        String accessToken = oAuthToken.getAccessToken();

        List<ProviderAccountDTO> accounts = new ArrayList<>();

        String externalAccountId = null;
        try {
            for (AccountV2 account : getAccounts(httpClient, accessToken)) {
                externalAccountId = account.getAccountUid();

                AccountIdentifiersV2 accountIdentifiers = getIdentifiersResponse(httpClient, accessToken, account);
                BalancesResponseV2 balancesResponse = getBalanceResponse(httpClient, accessToken, account);
                TransactionsResponseV2 transactionsResponse = getTransactionsResponse(httpClient, accessToken, account, fetchStartTime);
                AccountHolderNameV2 accountHolderName = getHolderNameResponse(httpClient, accessToken);

                ProviderAccountDTO providerAccountDTO = StarlingBankDataMapperV3
                        .convertAccount(account, accountIdentifiers, balancesResponse, transactionsResponse, accountHolderName, clock);

                accounts.add(providerAccountDTO);
            }
        } catch (RuntimeException e) {
            throw new ProviderFetchDataException(e);
        }
        return new DataProviderResponse(accounts);
    }

    private List<AccountV2> getAccounts(StarlingBankHttpClient httpClient, String accessToken) throws TokenInvalidException {
        AccountsResponseV2 accountsResponse = httpClient.fetchAccounts(GET_ACCOUNTS_URL, accessToken);
        return accountsResponse.getAccounts();
    }

    private AccountHolderNameV2 getHolderNameResponse(StarlingBankHttpClient httpClient, String accessToken) throws TokenInvalidException {
        return httpClient.fetchHolderName(GET_HOLDER_NAME_URL, accessToken);
    }

    private BalancesResponseV2 getBalanceResponse(StarlingBankHttpClient httpClient, String accessToken, AccountV2 account) throws TokenInvalidException {
        String url = String.format(GET_BALANCE_TEMPLATE, account.getAccountUid());
        return httpClient.fetchBalances(url, accessToken);
    }

    private AccountIdentifiersV2 getIdentifiersResponse(StarlingBankHttpClient httpClient, String accessToken, AccountV2 account) throws TokenInvalidException {
        String url = String.format(GET_ACCOUNTS_IDENTIFIERS_TEMPLATE, account.getAccountUid());
        return httpClient.fetchIdentifiers(url, accessToken);
    }

    private TransactionsResponseV2 getTransactionsResponse(StarlingBankHttpClient httpClient, String accessToken, AccountV2 account, Instant fetchStartTime) throws TokenInvalidException {
        String url = String.format(GET_TRANSACTIONS_TEMPLATE, account.getAccountUid(), account.getDefaultCategory());
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        String sinceTime = ZonedDateTime
                .ofInstant(fetchStartTime.truncatedTo(ChronoUnit.MILLIS), ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String now = ZonedDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS)
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        builder.queryParam("minTransactionTimestamp", sinceTime);
        builder.queryParam("maxTransactionTimestamp", now);

        return httpClient.fetchTransactions(builder.toUriString(), accessToken);
    }
}
