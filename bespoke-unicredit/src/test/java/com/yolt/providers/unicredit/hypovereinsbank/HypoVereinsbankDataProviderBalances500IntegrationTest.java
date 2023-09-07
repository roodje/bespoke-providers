package com.yolt.providers.unicredit.hypovereinsbank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.FakeRestTemplateManager;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.unicredit.AccessMeansTestMapper;
import com.yolt.providers.unicredit.TestApp;
import com.yolt.providers.unicredit.UnicreditSampleTypedAuthenticationMeans;
import com.yolt.providers.unicredit.common.ais.UniCreditDataProvider;
import com.yolt.providers.unicredit.common.dto.UniCreditAccessMeansDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/unicredit/hypovereins/balances-500", httpsPort = 0, port = 0)
@ActiveProfiles("unicredit")
public class HypoVereinsbankDataProviderBalances500IntegrationTest {

    private static final String CERT_PATH = "certificates/unicredit/unicredit_certificate.pem";
    private static final String PSU_IP_ADDRESS = "10.0.0.2";
    public static final String ACCOUNT1_ID = "312fdgyiuy374yr349fh8923hf0823h8757973482qop238rh39fh3920340923u";
    public static final String ACCOUNT2_ID = "980981309fdus09fu12uidhy90128123897dasd8012ud0823h832hf08345h09f";

    @Mock
    private Signer signer;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private RestTemplateManager restTemplateManager;

    private UnicreditSampleTypedAuthenticationMeans testAuthenticationMeans;

    @Qualifier("HypoVereinsbankDataProvider")
    @Autowired
    private UniCreditDataProvider dataProvider;

    @Autowired
    @Qualifier("Unicredit")
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() throws Exception {
        testAuthenticationMeans = new UnicreditSampleTypedAuthenticationMeans(CERT_PATH);
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @Test
    public void shouldThrowProviderFetchDataExceptionForFetchDataWhenInternalServerErrorOnBalancesAPICall() {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(UUID.randomUUID(),
                AccessMeansTestMapper.with(objectMapper).compactAccessMeans(new UniCreditAccessMeansDTO(UUID.randomUUID().toString(), Instant.now(), Instant.now())),
                new Date(),
                new Date());
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(testAuthenticationMeans.getAuthMeans())
                .setSigner(signer)
                .setTransactionsFetchStartTime(ZonedDateTime.of(2020, 9, 1, 0, 0, 0, 0, ZoneId.of("Z")).toInstant())
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.fetchData(request);

        // then
        assertThatExceptionOfType(ProviderFetchDataException.class)
                .isThrownBy(fetchDataCallable)
                .withMessage("Failed fetching data");
    }
}
