package com.yolt.providers.cbiglobe.common.service;

import com.yolt.providers.cbiglobe.common.model.CbiGlobeAccessMeansDTO;
import com.yolt.providers.cbiglobe.common.model.SignatureData;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

public interface CbiGlobeFetchDataService {

    List<ProviderAccountDTO> fetchAccounts(RestTemplate restTemplate,
                                           CbiGlobeAccessMeansDTO accessMeans,
                                           SignatureData signatureData,
                                           String aspspCode,
                                           String psuIpAddress);

    DataProviderResponse fetchTransactionsForAccounts(RestTemplate restTemplate,
                                                      CbiGlobeAccessMeansDTO accessMeans,
                                                      Instant transactionsFetchStartTime,
                                                      SignatureData signatureData,
                                                      String aspspCode,
                                                      String psuIpAddress) throws ProviderFetchDataException, TokenInvalidException;
}
