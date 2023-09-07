package com.yolt.providers.redsys.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.redsys.common.auth.RedsysAuthenticationMeans;
import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import com.yolt.providers.redsys.common.dto.AccountDetails;
import com.yolt.providers.redsys.common.dto.Balance;
import com.yolt.providers.redsys.common.dto.ResponseAccountsList;
import com.yolt.providers.redsys.common.model.RedsysAccessMeans;
import com.yolt.providers.redsys.common.model.SignatureData;
import com.yolt.providers.redsys.common.rest.BookingStatus;
import com.yolt.providers.redsys.common.rest.RedsysHttpClient;
import com.yolt.providers.redsys.common.rest.RedsysRestTemplateService;
import com.yolt.providers.redsys.common.service.mapper.RedsysDataMapperService;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.List;

import static com.yolt.providers.redsys.common.util.ErrorHandlerUtil.handleNon2xxResponseInFetchData;

@Slf4j
public class RedsysFetchDataServiceV3 extends RedsysFetchDataServiceV2 {

    private final RedsysRestTemplateService restTemplateService;
    private final RedsysDataMapperService mapperService;
    private final TransactionsFetchStartTime transactionsFetchStartTime;

    public RedsysFetchDataServiceV3(RedsysRestTemplateService restTemplateService,
                                    RedsysBaseProperties properties,
                                    RedsysDataMapperService mapperService,
                                    BookingStatus bookingStatus,
                                    TransactionsFetchStartTime transactionsFetchStartTime) {
        super(restTemplateService, properties, mapperService, bookingStatus, transactionsFetchStartTime);
        this.restTemplateService = restTemplateService;
        this.mapperService = mapperService;
        this.transactionsFetchStartTime = transactionsFetchStartTime;
    }

    @Override
    public DataProviderResponse fetchData(final RedsysAuthenticationMeans authenticationMeans,
                                          final RedsysAccessMeans accessMeans,
                                          final String providerName,
                                          final UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        final RestTemplateManager restTemplateManager = urlFetchData.getRestTemplateManager();
        final SignatureData signatureData = authenticationMeans.getSigningData(urlFetchData.getSigner());
        RedsysHttpClient httpClient = restTemplateService.createHttpClient(authenticationMeans, restTemplateManager);

        String accessToken = accessMeans.getToken().getAccessToken();
        String consentId = accessMeans.getConsentId();
        String psuIpAddress = urlFetchData.getPsuIpAddress();

        List<ProviderAccountDTO> accounts = new ArrayList<>();

        ResponseAccountsList listAccountReference = new ResponseAccountsList();

        try {
            listAccountReference = httpClient.getAllUserAccountsAndBalances(
                    accessMeans.getToken().getAccessToken(), accessMeans.getConsentId(), signatureData, psuIpAddress);
        } catch (HttpStatusCodeException e) {
            handleNon2xxResponseInFetchData(e, psuIpAddress);
        }

        for (AccountDetails account : listAccountReference.getAccounts()) {
            try {
                List<ProviderTransactionDTO> transactionsConverted = fetchTransactionsForAccount(httpClient, accessMeans, account,
                        transactionsFetchStartTime.calculate(accessMeans.getConsentAt(), urlFetchData.getTransactionsFetchStartTime()), signatureData, psuIpAddress);
                List<Balance> balances = account.getBalances();
                if (balances == null || balances.isEmpty()) {
                    balances = httpClient.getBalanceForAccount(accessToken, consentId, account.getResourceId(), signatureData, psuIpAddress).getBalances();
                }
                accounts.add(mapperService.toProviderAccountDTO(account, balances, transactionsConverted, providerName));
            } catch (HttpStatusCodeException e) {
                handleNon2xxResponseInFetchData(e, psuIpAddress);
            } catch (RuntimeException e) {
                throw new ProviderFetchDataException(e);
            }
        }
        return new DataProviderResponse(accounts);
    }
}
