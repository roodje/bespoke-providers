package com.yolt.providers.openbanking.ais.bankofirelandgroup.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.BankOfIrelandGroupApp;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.BankOfIrelandRoiSampleTypedAuthMeans;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.BankOfIrelandSampleTypedAuthMeans;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.BankOfIrelandGroupBaseDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains fetch data flow when errors occur on optional endpoints in Bank of Ireland provider.
 * <p>
 * Covered flows:
 * - fetching accounts, balances, transactions, standing orders
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BankOfIrelandGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/bankofireland/ais-3.0.0/error-on-optional-endpoints", httpsPort = 0, port = 0)
@ActiveProfiles("bankofireland")
public class BankOfIrelandGroupDataProviderFetchDataOptionalEndpointsErrorIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "http://yolt.com/identifier";
    private static String SERIALIZED_ACCESS_MEANS;
    private static final Signer SIGNER = new SignerMock();

    private RestTemplateManagerMock restTemplateManagerMock;
    private String requestTraceId;

    @Autowired
    @Qualifier("BankOfIrelandDataProviderV7")
    private BankOfIrelandGroupBaseDataProvider bankOfIrelandDataProviderV7;

    @Autowired
    @Qualifier("BankOfIrelandRoiDataProvider")
    private BankOfIrelandGroupBaseDataProvider bankOfIrelandRoiDataProvider;

    private Stream<Arguments> getProvidersWithSampleAuthMeans() {
        return Stream.of(
                Arguments.of(bankOfIrelandDataProviderV7, BankOfIrelandSampleTypedAuthMeans.getSampleAuthMeans()),
                Arguments.of(bankOfIrelandRoiDataProvider, BankOfIrelandRoiSampleTypedAuthMeans.getSampleAuthMeans())
        );
    }

    @BeforeAll
    public static void beforeAll() throws JsonProcessingException {
        AccessMeans accessMeans = new AccessMeans(
                Instant.now(),
                USER_ID,
                "j4HmjDMaBdSXUQQzN1GpdHoozGho",
                "xcdog81ShrtxuIqRO9Zlf0zG0YVuaGuo9iK8sHWeA8",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL
        );
        SERIALIZED_ACCESS_MEANS = OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(accessMeans);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        requestTraceId = "5008e82d-f5ac-42fe-8f07-a49b025f3c2e";
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithSampleAuthMeans")
    public void shouldNotThrowExceptionWhenErrorOccursOnOptionalEndpoints(BankOfIrelandGroupBaseDataProvider subject, Map<String, BasicAuthenticationMean> authenticationMeans) throws Exception {
        // given
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(createAccessMeansDTO(SERIALIZED_ACCESS_MEANS))
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        DataProviderResponse dataProviderResponse = subject.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts().size()).isEqualTo(1);
    }

    private AccessMeansDTO createAccessMeansDTO(String accessMeans) {
        return new AccessMeansDTO(USER_ID, accessMeans, new Date(),
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
    }
}