package com.yolt.providers.openbanking.ais.tescobank.ais.v3.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.tescobank.TescoBankApp;
import com.yolt.providers.openbanking.ais.tescobank.TescoSampleAccessMeansV2;
import com.yolt.providers.openbanking.ais.tescobank.TescoSampleTypedAuthenticationMeansV2;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains case according to documentation, when request to accounts returned 504.
 * This means that bank doesn't respond on time, thus we want to inform user that fetch data failed (so throw {@link ProviderFetchDataException})
 * <p>
 * Covered flows:
 * - fetching accounts
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TescoBankApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/tescobank/ais-3.1/accounts-504", httpsPort = 0, port = 0)
@ActiveProfiles("tescobank")
public class TescoDataProviderFetchData504IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final Signer SIGNER = new SignerMock();
    private static final String REQUEST_TRACE_ID = UUID.randomUUID().toString();

    private static String SERIALIZED_ACCESS_MEANS;

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
    public void shouldThrowProviderFetchDataExceptionOn504(GenericBaseDataProviderV2 provider) throws IOException, URISyntaxException {
        // given
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(createAccessMeansDTO(SERIALIZED_ACCESS_MEANS))
                .setAuthenticationMeans(TescoSampleTypedAuthenticationMeansV2.getTypedAuthenticationMeans())
                .setRestTemplateManager(new RestTemplateManagerMock(() -> REQUEST_TRACE_ID))
                .setSigner(SIGNER)
                .build();

        // when
        ThrowableAssert.ThrowingCallable handler = () -> provider.fetchData(urlFetchData);

        // then
        assertThatThrownBy(handler).isExactlyInstanceOf(ProviderFetchDataException.class);
    }

    private AccessMeansDTO createAccessMeansDTO(String accessMeans) {
        return new AccessMeansDTO(USER_ID, accessMeans, new Date(),
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
    }
}
