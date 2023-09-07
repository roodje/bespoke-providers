package com.yolt.providers.stet.generic.service.fetchdata.account;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class FetchDeclaredAccountsStrategy extends FetchBasicAccountsStrategy {

    public FetchDeclaredAccountsStrategy(FetchDataRestClient restClient) {
        super(restClient);
    }

    @Override
    public List<StetAccountDTO> fetchAccounts(HttpClient httpClient,
                                              String accountsEndpoint,
                                              String consentsEndpoint,
                                              DataRequest dataRequest) throws TokenInvalidException {
        List<StetAccountDTO> accountDTOs = super.fetchAccounts(httpClient, accountsEndpoint, consentsEndpoint, dataRequest);
        if (!CollectionUtils.isEmpty(accountDTOs)) {
            Map<String, Object> body = createConsentUpdateBody(accountDTOs);
            restClient.updateConsent(httpClient, consentsEndpoint, dataRequest, body);
            return super.fetchAccounts(httpClient, accountsEndpoint, consentsEndpoint, dataRequest);
        }
        return Collections.emptyList();
    }

    protected Map<String, Object> createConsentUpdateBody(List<StetAccountDTO> accounts) {
        Map<String, Object> payload = new HashMap<>();
        List<Map<String, Object>> balanceTransactionPayload = accounts.stream()
                .map(account -> {
                    Map<String, Object> consentDetails = new HashMap<>();
                    consentDetails.put("iban", account.getIban());
                    consentDetails.put("other", account.getOther());
                    consentDetails.put("currency", account.getCurrency());
                    if (Objects.nonNull(account.getArea())) {
                        consentDetails.put("area", account.getArea());
                    }
                    return consentDetails;
                })
                .collect(Collectors.toList());

        payload.put("psuIdentity", "true");
        payload.put("trustedBeneficiaries", "false");
        payload.put("balances", balanceTransactionPayload);
        payload.put("transactions", balanceTransactionPayload);
        return payload;
    }
}
