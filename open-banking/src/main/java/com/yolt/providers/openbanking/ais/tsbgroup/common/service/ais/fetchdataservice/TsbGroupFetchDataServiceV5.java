package com.yolt.providers.openbanking.ais.tsbgroup.common.service.ais.fetchdataservice;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBTransaction6;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;

import java.time.Clock;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;


public class TsbGroupFetchDataServiceV5 extends DefaultFetchDataService {


    public TsbGroupFetchDataServiceV5(RestClient restClient,
                                      DefaultProperties properties,
                                      Function<OBTransaction6, ProviderTransactionDTO> transactionMapper,
                                      DefaultAccountMapper accountMapper,
                                      UnaryOperator<List<OBAccount6>> accountFilter,
                                      Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier,
                                      Duration consentWindow,
                                      String endpointsVersion,
                                      Clock clock) {
        super(restClient, properties, transactionMapper, null, null, accountMapper, accountFilter, supportedAccountSupplier, consentWindow, endpointsVersion, clock);
    }

    @Override
    protected List<DirectDebitDTO> getDirectDebits(HttpClient httpClient, AccessMeans accessToken, String externalAccountId, DefaultAuthMeans authenticationMeans, OBExternalAccountSubType1Code accountSubType) {
        //We are not using the direct debits and as TSB is struggling with processing requests, we are removing this requests.
        return Collections.emptyList();
    }

    @Override
    protected List<StandingOrderDTO> getStandingOrders(HttpClient httpClient, AccessMeans accessToken, String externalAccountId, DefaultAuthMeans authenticationMeans, OBExternalAccountSubType1Code accountSubType) {
        //We are not using the direct debits and as TSB is struggling with processing requests, we are removing this requests.
        return Collections.emptyList();
    }

}
