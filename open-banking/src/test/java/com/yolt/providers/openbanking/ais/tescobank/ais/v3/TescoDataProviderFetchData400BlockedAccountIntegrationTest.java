package com.yolt.providers.openbanking.ais.tescobank.ais.v3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.tescobank.TescoBankApp;
import com.yolt.providers.openbanking.ais.tescobank.TescoSampleAccessMeansV2;
import com.yolt.providers.openbanking.ais.tescobank.TescoSampleTypedAuthenticationMeansV2;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * This test contains case according to documentation, when request to accounts returned 401.
 * This means that request is unauthorized, thus we want to force user to fill a consent (so throw {@link TokenInvalidException})
 * <p>
 * Covered flows:
 * - fetching accounts
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TescoBankApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/tescobank/ais-3.1/transactions-400-closed", httpsPort = 0, port = 0)
@ActiveProfiles("tescobank")
public class TescoDataProviderFetchData400BlockedAccountIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static String SERIALIZED_ACCESS_MEANS;
    private static final Signer SIGNER = new SignerMock();
    private static final String REQUEST_TRACE_ID = UUID.randomUUID().toString();

    @Autowired
    @Qualifier("TescoBankDataProviderV7")
    private GenericBaseDataProviderV2 tescoBankDataProviderV7;

    private Stream<GenericBaseDataProviderV2> getProviders() {
        return Stream.of(tescoBankDataProviderV7);
    }

    @BeforeAll
    public static void setup() throws JsonProcessingException {
        SERIALIZED_ACCESS_MEANS = TescoSampleAccessMeansV2.getSerializedAccessMeans();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnOneAccountWhenReceivedBadRequestForBlockedAccount(GenericBaseDataProviderV2 provider) throws IOException, URISyntaxException, TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(createAccessMeansDTO(SERIALIZED_ACCESS_MEANS))
                .setAuthenticationMeans(TescoSampleTypedAuthenticationMeansV2.getTypedAuthenticationMeans())
                .setRestTemplateManager(new RestTemplateManagerMock(() -> REQUEST_TRACE_ID))
                .setSigner(SIGNER)
                .build();

        // when
        DataProviderResponse response = provider.fetchData(urlFetchData);

        // then
        assertThat(response.getAccounts()).hasSize(1);
        assertThat(response.getAccounts().get(0).getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
    }

    private AccessMeansDTO createAccessMeansDTO(String accessMeans) {
        return new AccessMeansDTO(USER_ID, accessMeans, new Date(),
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
    }
}
