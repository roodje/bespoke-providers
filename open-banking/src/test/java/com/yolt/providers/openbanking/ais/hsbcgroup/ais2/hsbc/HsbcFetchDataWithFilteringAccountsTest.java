package com.yolt.providers.openbanking.ais.hsbcgroup.ais2.hsbc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupApp;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupSampleAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.HsbcGroupBaseDataProviderV7;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.model.HsbcGroupAccessMeansV2;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {HsbcGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("hsbc-generic")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/hsbcgroup/ais-3.1.6/hsbc"}, httpsPort = 0, port = 0)
public class HsbcFetchDataWithFilteringAccountsTest {

    private static final ObjectMapper OBJECT_MAPPER = OpenBankingTestObjectMapper.INSTANCE;
    private static final SignerMock SIGNER = new SignerMock();

    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final Instant FETCH_FROM = Instant.parse("2020-01-01T00:00:00Z");
    private static final String ACCESS_TOKEN = "3zHagXZSPyA6sifIDmqwZ0hLlqq";
    private static final String REFRESH_TOKEN = "NfEtxxaLt1SavZW1s7thJ7iw0XZ";
    private static final String BLOCKED_ACCOUNT_ID = "l8zlqg6kt9moi1f5pu5jfh43p68s3c1l3ca6";
    private HsbcGroupSampleAuthenticationMeansV2 sampleAuthenticationMeans = new HsbcGroupSampleAuthenticationMeansV2();

    @Autowired
    @Qualifier("HsbcDataProviderV13")
    HsbcGroupBaseDataProviderV7 provider;

    private String requestTraceId;
    private RestTemplateManager restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    public void setUp() throws IOException, URISyntaxException {
        requestTraceId = "d10f24f4-032a-4843-bfc9-22b599c7ae2d";
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        authenticationMeans = sampleAuthenticationMeans.getHsbcGroupSampleAuthenticationMeansForAis();

    }

    @Test
    public void shouldFetchDataWithoutBlockedAccounts() throws JsonProcessingException, TokenInvalidException, ProviderFetchDataException {
        //given
        AccessMeansState<HsbcGroupAccessMeansV2> hsbcGroupAccessMeans = createTokenWithoutPermissions(ACCESS_TOKEN);
        UrlFetchDataRequest urlFetchDataRequest = createUrlFetchDataRequest(createAccessMeansDTO(hsbcGroupAccessMeans));
        //when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchDataRequest);

        //then
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        Assertions.assertThat(accounts).hasSize(1);
        Assertions.assertThat(accounts
                .stream()
                .map(account -> account.getAccountId())
                .collect(Collectors.toList())).doesNotContain(BLOCKED_ACCOUNT_ID);
    }

    private AccessMeansState<HsbcGroupAccessMeansV2> createTokenWithoutPermissions(String accessToken) {
        return new AccessMeansState<>(
                new HsbcGroupAccessMeansV2(
                        Instant.now(),
                        USER_ID,
                        accessToken,
                        REFRESH_TOKEN,
                        Date.from(Instant.now().plus(1, DAYS)),
                        Date.from(Instant.now()),
                        "https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0"),
                Collections.emptyList());
    }

    private AccessMeansDTO createAccessMeansDTO(AccessMeansState<HsbcGroupAccessMeansV2> oAuthToken) throws JsonProcessingException {
        return new AccessMeansDTO(
                USER_ID,
                OBJECT_MAPPER.writeValueAsString(oAuthToken),
                new Date(),
                Date.from(Instant.now().plus(1, DAYS)));
    }

    private UrlFetchDataRequest createUrlFetchDataRequest(final AccessMeansDTO accessMeansDTO) {
        return new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setTransactionsFetchStartTime(FETCH_FROM)
                .setAccessMeans(accessMeansDTO)
                .setSigner(SIGNER)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();
    }
}
