package com.yolt.providers.stet.bpcegroup.common.service.fetchdata.account;

import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.service.fetchdata.account.FetchDeclaredAccountsStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class BpceGroupFetchDeclaredAccountsStrategy extends FetchDeclaredAccountsStrategy {

    public BpceGroupFetchDeclaredAccountsStrategy(FetchDataRestClient restClient) {
        super(restClient);
    }

    @Override
    public List<StetAccountDTO> fetchAccounts(HttpClient httpClient,
                                              String accountsEndpoint,
                                              String consentsEndpoint,
                                              DataRequest dataRequest) throws TokenInvalidException {
        List<StetAccountDTO> accountDTOs;
        try {
            accountDTOs = super.fetchAccounts(httpClient, accountsEndpoint, consentsEndpoint, dataRequest);
        } catch (ProviderHttpStatusException exception) {
            if (exception.getMessage().contains("NAAC No available accounts : no eligible or authorized accounts")) {
                return Collections.emptyList();
            }
            throw exception;
        }
        if (!CollectionUtils.isEmpty(accountDTOs)) {
            Map<String, Object> body = createConsentUpdateBody(accountDTOs);
            restClient.updateConsent(httpClient, consentsEndpoint, dataRequest, body);
            return super.fetchAccounts(httpClient, accountsEndpoint, consentsEndpoint, dataRequest);
        }
        return Collections.emptyList();
    }

    @Override
    protected Map<String, Object> createConsentUpdateBody(List<StetAccountDTO> accounts) {
        Map<String, Object> payload = new LinkedHashMap<>();
        List<Map<String, Object>> balanceTransactionPayload = accounts.stream()
                .map(account -> {
                    Map<String, Object> consentDetails = new HashMap<>();
                    consentDetails.put("iban", account.getIban());
                    if (Objects.nonNull(account.getOther()) && !account.getOther().isEmpty()) {
                        consentDetails.put("other", account.getOther());
                    }
                    return consentDetails;
                })
                .collect(Collectors.toList());

        payload.put("balances", balanceTransactionPayload);
        payload.put("transactions", balanceTransactionPayload);
        payload.put("trustedBeneficiaries", false);
        payload.put("psuIdentity", true);
        return payload;
    }
}
