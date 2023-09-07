package com.yolt.providers.openbanking.ais.generic2.http;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericApp;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeansBuilder;
import com.yolt.providers.openbanking.ais.generic2.configuration.auth.GenericSampleTypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadAccount6;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {GenericApp.class, OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/generic/bad_responses", port = 0, httpsPort = 0)
@ActiveProfiles("generic2")
public class GenericDefaultHttpClientJsonMismatchIntegrationTest {

    private static final String PROVIDER_KEY = "TEST_IMPL_OPENBANKING";
    private RestTemplateManagerMock restTemplateManagerMock;
    private DefaultAuthMeans authMeans;
    private DefaultRestClient restClient;
    private HttpClient httpClient;
    private AccessMeans accessMeans;

    @Autowired
    private HttpClientFactory httpClientFactory;

    @Test
    public void shouldThrowRootCauseMismatchedInputExceptionDueToCorruptedJsonResponse() throws IOException, URISyntaxException {

        // given
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");

        authMeans = new DefaultAuthMeansBuilder().createAuthenticationMeans(new GenericSampleTypedAuthenticationMeans().getAuthenticationMeans(), PROVIDER_KEY);

        httpClient = httpClientFactory.createHttpClient(restTemplateManagerMock, authMeans, "any");

        restClient = new DefaultRestClient(new ExternalPaymentRequestSigner(OpenBankingTestObjectMapper.INSTANCE, AlgorithmIdentifiers.RSA_PSS_USING_SHA256));

        accessMeans = new AccessMeans(
                Instant.now(),
                UUID.randomUUID(),
                "accessToken000-0",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                "https://www.test-url.com/");

        // when
        assertThatThrownBy(() -> restClient.fetchAccounts(httpClient, "/aisp/accounts", accessMeans,
                "test", OBReadAccount6.class))
                .hasRootCauseInstanceOf(MismatchedInputException.class)
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("JSON parse error");
    }
}