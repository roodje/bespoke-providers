package com.yolt.providers.brdgroup.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.brdgroup.BrdGroupSampleAuthenticationMeans;
import com.yolt.providers.brdgroup.TestConfiguration;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import lombok.SneakyThrows;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = TestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/get-accounts-500/", httpsPort = 0, port = 0)
@ActiveProfiles("brd")
class BrdGroupDataProviderGetAccountsHttp500IntegrationTest {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final Date UPDATED_DATE = parseDate("2020-01-02");
    private static final Date EXPIRATION_DATE = parseDate("2020-01-03");
    private static final String CONSENT_ID = "800000022";

    @Autowired
    @Qualifier("BrdGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("BrdDataProviderV1")
    private BrdGroupDataProvider dataProvider;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans = BrdGroupSampleAuthenticationMeans.get();

    @Test
    void shouldThrowProviderFetchDataExceptionOnHttp500() {
        // given
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAccessMeans(accessMeansDTO())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(Instant.now())
                .build();

        // when
        assertThatThrownBy(() -> dataProvider.fetchData(request))
                .isExactlyInstanceOf(ProviderHttpStatusException.class);
    }

    private static Date parseDate(String date) {
        return Date.from(LocalDate.parse(date).atStartOfDay().toInstant(UTC));
    }

    @SneakyThrows
    private AccessMeansDTO accessMeansDTO() {
        BrdGroupAccessMeans brdGroupAccessMeans = new BrdGroupAccessMeans(CONSENT_ID);
        return new AccessMeansDTO(USER_ID, objectMapper.writeValueAsString(brdGroupAccessMeans), UPDATED_DATE, EXPIRATION_DATE);
    }
}