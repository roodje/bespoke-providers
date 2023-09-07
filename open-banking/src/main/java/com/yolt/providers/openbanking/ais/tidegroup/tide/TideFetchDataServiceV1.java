package com.yolt.providers.openbanking.ais.tidegroup.tide;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class TideFetchDataServiceV1 extends DefaultFetchDataService {

    private final Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier;

    public TideFetchDataServiceV1(RestClient restClient, DefaultProperties properties, Function<OBTransaction6, ProviderTransactionDTO> transactionMapper, Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper, Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper, AccountMapper accountMapper, UnaryOperator<List<OBAccount6>> accountFilter, Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier, Duration consentWindow, String endpointsVersion, Clock clock) {
        super(restClient, properties, transactionMapper, directDebitMapper, standingOrderMapper, accountMapper, accountFilter, supportedAccountSupplier, consentWindow, endpointsVersion, clock);
        this.supportedAccountSupplier = supportedAccountSupplier;
    }

    @Override
    protected void checkAndProcessAccount(final HttpClient httpClient,
                                          final DefaultAuthMeans authenticationMeans,
                                          final Instant transactionsFetchStartTime,
                                          final AccessMeans accessToken,
                                          final List<ProviderAccountDTO> responseAccounts,
                                          final OBAccount6 account) throws TokenInvalidException {
        String externalAccountId = account.getAccountId();
        if (supportedAccountSupplier.get().contains(account.getAccountSubType())) {
            ProviderAccountDTO providerAccountDto = processAccount(httpClient, transactionsFetchStartTime, accessToken,
                    externalAccountId, account, authenticationMeans);

            // C4PO-10318 At this moment bank returns empty balances data when an account is closed/blocked.
            // Bank is working on a fix, after which both the accounts and balances endpoint would return empty data.
            // After fix, check bank answer and change on account level.
            if (providerAccountDto.getAvailableBalance() != null || providerAccountDto.getCurrentBalance() != null) {
                responseAccounts.add(providerAccountDto);
            }
        }
    }
}
