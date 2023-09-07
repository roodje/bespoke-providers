package com.yolt.providers.stet.cmarkeagroup;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.stet.cmarkeagroup.common.CmArkeaGroupDataProvider;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = CmArkeaGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("cmarkeagroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/cmarkea/ais/1.4.2", httpsPort = 0, port = 0)
public class CmArkeaGroupDataProviderV3TwoAccountsIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private final Signer signer = mock(Signer.class);

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("FortuneoDataProviderV3")
    private CmArkeaGroupDataProvider fortuneoDataProviderV3;

    @Autowired
    @Qualifier("AxaDataProviderV3")
    private CmArkeaGroupDataProvider axaDataProviderV3;

    @Autowired
    @Qualifier("AllianzBanqueDataProviderV1")
    private CmArkeaGroupDataProvider allianzBanqueDataProviderV1;

    @Autowired
    @Qualifier("MaxBankDataProviderV1")
    private CmArkeaGroupDataProvider maxBankDataProviderV1;

    @Autowired
    @Qualifier("CreditMutuelDuSudOuestDataProviderV1")
    private CmArkeaGroupDataProvider creditMutuelDuSudOuestDataProviderV1;

    @Autowired
    @Qualifier("ArkeaBanqueEntreprisesDataProviderV1")
    private CmArkeaGroupDataProvider arkeaBanqueEntreprisesDataProviderV1;

    @Autowired
    @Qualifier("ArkeaBanquePriveeDataProviderV1")
    private CmArkeaGroupDataProvider arkeaBanquePriveeDataProviderV1;

    @Autowired
    @Qualifier("ArkeaBankingServicesDataProviderV1")
    private CmArkeaGroupDataProvider arkeaBankingServicesDataProviderV1;

    @Autowired
    @Qualifier("BpeDataProviderV1")
    private CmArkeaGroupDataProvider bpeDataProviderV1;

    @Autowired
    @Qualifier("CreditMutuelDeBretagneDataProviderV1")
    private CmArkeaGroupDataProvider creditMutuelDeBretagneDataProviderV1;

    @Autowired
    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(fortuneoDataProviderV3, axaDataProviderV3, allianzBanqueDataProviderV1, maxBankDataProviderV1,
                creditMutuelDuSudOuestDataProviderV1, creditMutuelDeBretagneDataProviderV1, arkeaBanqueEntreprisesDataProviderV1,
                arkeaBanquePriveeDataProviderV1, arkeaBankingServicesDataProviderV1, bpeDataProviderV1);
    }

    @Autowired
    private Clock clock;

    @BeforeEach
    public void setUp() {
        when(signer.sign(ArgumentMatchers.any(byte[].class), any(), ArgumentMatchers.any(SignatureAlgorithm.class)))
                .thenReturn(Base64.toBase64String("TEST-ENCODED-SIGNATURE".getBytes()));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnTwoAccounts(UrlDataProvider dataProvider) throws ProviderFetchDataException, TokenInvalidException {
        // given
        String accessMeans = "{\"expires_in\":3600,\"access_token\":\"accessToken123456701\",\"refresh_token\":\"refresh-token\", \"refreshed\":\"false\"}";

        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(
                USER_ID,
                accessMeans,
                new Date(),
                new Date());

        UrlFetchDataRequest request = createFetchDataRequest(accessMeansDTO);

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(request);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(2);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);
    }

    private UrlFetchDataRequest createFetchDataRequest(AccessMeansDTO accessMeansDTO) {
        return new UrlFetchDataRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(CmArkeaGroupSampleMeans.createTestAuthenticationMeans())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setTransactionsFetchStartTime(Instant.now(clock))
                .build();
    }
}
