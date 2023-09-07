package com.yolt.providers.openbanking.ais.kbciegroup.common.service.fetchdataservice;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataServiceV3;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountMapperV2;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.PartiesRestClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBTransaction6;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class KbcIeGroupFetchDataService extends DefaultFetchDataServiceV3 {

    public KbcIeGroupFetchDataService(RestClient restClient,
                                      PartiesRestClient partiesRestClient,
                                      DefaultProperties properties,
                                      Function<OBTransaction6, ProviderTransactionDTO> transactionMapper,
                                      AccountMapperV2 accountMapper,
                                      Function<Instant, String> fromBookingDateTimeFormatter,
                                      UnaryOperator<List<OBAccount6>> accountFilter,
                                      Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier,
                                      Duration consentWindow,
                                      String endpointsVersion,
                                      Clock clock) {

        super(restClient, partiesRestClient, properties, transactionMapper, null, null, null, accountMapper, fromBookingDateTimeFormatter, accountFilter, supportedAccountSupplier, consentWindow, endpointsVersion, clock);
    }

    @Override
    protected List<PartyDto> getParties(HttpClient httpClient, AccessMeansState accessMeansState, String externalAccountId, DefaultAuthMeans authenticationMeans) {
        return Collections.emptyList();
    }

    @Override
    protected List<DirectDebitDTO> getDirectDebits(HttpClient httpClient, AccessMeans accessToken, String externalAccountId, DefaultAuthMeans authenticationMeans, OBExternalAccountSubType1Code accountSubType) {
        return Collections.emptyList();
    }

    @Override
    protected List<StandingOrderDTO> getStandingOrders(HttpClient httpClient, AccessMeans accessToken, String externalAccountId, DefaultAuthMeans authenticationMeans, OBExternalAccountSubType1Code accountSubType) {
        return Collections.emptyList();
    }
}
