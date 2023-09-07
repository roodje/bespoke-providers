package com.yolt.providers.nutmeggroup.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.nutmeggroup.common.dto.Pot;
import com.yolt.providers.nutmeggroup.common.dto.PotsResponse;
import com.yolt.providers.nutmeggroup.common.dto.TokenResponse;
import com.yolt.providers.nutmeggroup.common.rest.HttpClient;
import com.yolt.providers.nutmeggroup.common.rest.NutmegGroupRestTemplateServiceV2;
import com.yolt.providers.nutmeggroup.nutmeg.configuration.NutmegProperties;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static com.yolt.providers.nutmeggroup.common.DataMapper.mapToProviderAccountDTO;
import static com.yolt.providers.nutmeggroup.common.utils.SerializationUtils.fromJson;

@Service
public class NutmegGroupFetchDataServiceV2 {

    private final NutmegGroupRestTemplateServiceV2 restTemplateService;
    private final NutmegProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public NutmegGroupFetchDataServiceV2(final NutmegGroupRestTemplateServiceV2 restTemplateService,
                                         final NutmegProperties properties,
                                         @Qualifier("NutmegGroupObjectMapper") final ObjectMapper objectMapper,
                                         final Clock clock) {
        this.restTemplateService = restTemplateService;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public DataProviderResponse fetchdata(final UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        RestTemplateManager restTemplateManager = urlFetchData.getRestTemplateManager();
        HttpClient httpClient = restTemplateService.createHttpClient(restTemplateManager);

        List<ProviderAccountDTO> responseAccounts = new ArrayList<>();

        try {
            TokenResponse accessMeans = fromJson(objectMapper, urlFetchData.getAccessMeans().getAccessMeans());
            PotsResponse potsResponse = httpClient.getPots(accessMeans.getAccessToken(), properties.getPotsUrl());

            for (Pot pot : potsResponse.getPots()) {
                responseAccounts.add(mapToProviderAccountDTO(pot, clock));
            }
        } catch (RuntimeException e) {
            throw new ProviderFetchDataException(e);
        }

        return new DataProviderResponse(responseAccounts);
    }
}
