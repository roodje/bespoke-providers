package com.yolt.providers.commerzbankgroup.common.data.service;


import com.yolt.providers.commerzbankgroup.common.api.CommerzbankGroupApiClient;
import com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata.AccountDetails;
import com.yolt.providers.commerzbankgroup.common.data.mapper.CommerzbankGroupDataMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@AllArgsConstructor
public class CommerzbankGroupFetchDataService {

    private final CommerzbankGroupDataMapper dataMapper;

    public DataProviderResponse fetchDataSince(String accessToken,
                                               String consentId,
                                               Instant transactionsFetchStartTime,
                                               Supplier<CommerzbankGroupApiClient> apiClient) throws TokenInvalidException {
        var commerzbankGroupApiClient = apiClient.get();
        var consentCredentials = new CommerzbankGroupApiClient.ConsentCredentials(accessToken, consentId);
        var accountList = commerzbankGroupApiClient.fetchAccounts(consentCredentials);

        List<ProviderAccountDTO> providerAccountDTOs = new ArrayList<>();

        var localDate = transactionsFetchStartTime.atZone(ZoneId.of("Europe/Berlin")).toLocalDate();
        for (AccountDetails account : accountList.getAccounts()) {
            var transactionsResponse200Jsons = commerzbankGroupApiClient.fetchAllPagesOfTransactionsForAnAccount(consentCredentials, account.getResourceId(), localDate);
            var readAccountBalanceResponse200 = commerzbankGroupApiClient.fetchBalancesForAnAccount(consentCredentials, account.getResourceId());

            ProviderAccountDTO providerAccountDTO = dataMapper.toProviderAccountDTO(account, transactionsResponse200Jsons, readAccountBalanceResponse200);
            providerAccountDTOs.add(providerAccountDTO);

        }
        return new DataProviderResponse(providerAccountDTOs);
    }

}
