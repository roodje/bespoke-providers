package com.yolt.providers.yoltprovider;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.versioning.ProviderVersion;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_2;

/**
 * Provider adds additional flag to bank specific information in accounts and transactions
 * This is done to distinguish which provider version is used during cucumber tests
 */
public class YoltProviderVersion2 extends YoltProvider {

    public YoltProviderVersion2(final YoltProviderConfigurationProperties properties,
                                final YoltProviderFetchDataService yoltProviderFetchDataService,
                                final YoltProviderAuthorizationService yoltProviderAuthorizationService,
                                final Clock clock) {
        super(properties, yoltProviderFetchDataService, yoltProviderAuthorizationService, clock);
    }

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        DataProviderResponse dataProviderResponse = super.fetchData(urlFetchData);
        List<ProviderAccountDTO> enrichedAccountsWithBankSpecificExperimentalFlag = new ArrayList<>();
        dataProviderResponse.getAccounts().forEach(account -> {
            ProviderAccountDTO enrichedAccount = enrichDataWithBankSpecificExperimentalFlag(account);
            enrichedAccountsWithBankSpecificExperimentalFlag.add(enrichedAccount);
        });

        return new DataProviderResponse(enrichedAccountsWithBankSpecificExperimentalFlag);
    }

    private ProviderAccountDTO enrichDataWithBankSpecificExperimentalFlag(final ProviderAccountDTO account) {
        Map<String, String> bankSpecific = account.getBankSpecific() == null ? new HashMap<>() : account.getBankSpecific();
        bankSpecific.put("experimental", "true");
        return account.toBuilder()
                .bankSpecific(bankSpecific)
                .build();
    }

    private ProviderTransactionDTO enrichTransactionWithBankSpecificExperimentalFlag(final ProviderTransactionDTO transaction) {
        Map<String, String> bankSpecific = transaction.getBankSpecific() == null ? new HashMap<>() : transaction.getBankSpecific();
        bankSpecific.put("experimental", "true");
        return transaction.toBuilder()
                .bankSpecific(bankSpecific)
                .build();
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_2;
    }

}
