package com.yolt.providers.ing.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.common.util.SimpleRestTemplateManagerMock;
import com.yolt.providers.ing.TestApp;
import com.yolt.providers.ing.common.IngDataProviderV9;
import com.yolt.providers.ing.common.TestSigner;
import com.yolt.providers.ing.common.auth.IngAuthData;
import com.yolt.providers.ing.common.auth.IngClientAccessMeans;
import com.yolt.providers.ing.common.auth.IngUserAccessMeans;
import com.yolt.providers.ing.common.config.IngObjectMapper;
import com.yolt.providers.ing.common.dto.TestIngAuthData;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.KEY_ID;
import static com.yolt.providers.ing.common.auth.IngAuthenticationMeans.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/missing-data", httpsPort = 0, port = 0)
public class IngDataProviderIntegrationMissingDataTest {

    private static final String CLIENT_ID = "example_client_id";

    private static final String EXPECTED_ACCESS_TOKEN = "test-customer-access-token";
    private static final String EXPECTED_REFRESH_TOKEN = "test-customer-refresh-token";

    private static final AuthenticationMeansReference AUTHENTICATION_MEANS_REFERENCE = new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID());

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("IngDeDataProviderV10")
    private IngDataProviderV9 ingDeDataProviderV10;

    @Autowired
    @Qualifier("IngDeDataProviderV11")
    private IngDataProviderV9 ingDeDataProviderV11;

    @Autowired
    @Qualifier("IngBeDataProviderV10")
    private IngDataProviderV9 ingBeDataProviderV10;

    @Autowired
    @Qualifier("IngFrDataProviderV10")
    private IngDataProviderV9 ingFrDataProviderV10;

    @Autowired
    @Qualifier("IngItDataProviderV10")
    private IngDataProviderV9 ingItDataProviderV10;

    @Autowired
    @Qualifier("IngNlDataProviderV10")
    private IngDataProviderV9 ingNlDataProviderV10;

    @Autowired
    @Qualifier("IngRoDataProviderV10")
    private IngDataProviderV9 ingRoDataProviderV10;

    @Autowired
    private Clock clock;
    private ObjectMapper mapper;

    private RestTemplateManager restTemplateManager;
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    public void beforeEach() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        mapper = IngObjectMapper.get();
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadPemFile("example_client_signing.cer")));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadPemFile("example_client_tls.cer")));
        authenticationMeans.put(TRANSPORT_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), "00000000-0000-0000-0000-000000000000"));
        authenticationMeans.put(SIGNING_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), "11111111-1111-1111-1111-111111111111"));
        PrivateKey signingKey = KeyUtil.createPrivateKeyFromPemFormat((loadPemFile("example_client_signing.key")));
        ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory = new ExternalRestTemplateBuilderFactory();
        externalRestTemplateBuilderFactory.requestFactory(SimpleClientHttpRequestFactory::new);
        restTemplateManager = new SimpleRestTemplateManagerMock(externalRestTemplateBuilderFactory);
        signer = new TestSigner(signingKey);
    }

    public Stream<Arguments> getIngDataProviders() {
        return Stream.of(
                arguments(ingBeDataProviderV10.getProviderIdentifier(), ingBeDataProviderV10, ZoneId.of("Europe/Brussels")),
                arguments(ingBeDataProviderV10.getProviderIdentifier(), ingItDataProviderV10, ZoneId.of("Europe/Rome")),
                arguments(ingBeDataProviderV10.getProviderIdentifier(), ingFrDataProviderV10, ZoneId.of("Europe/Paris")),
                arguments(ingBeDataProviderV10.getProviderIdentifier(), ingNlDataProviderV10, ZoneId.of("Europe/Amsterdam")),
                arguments(ingBeDataProviderV10.getProviderIdentifier(), ingRoDataProviderV10, ZoneId.of("Europe/Bucharest")),
                arguments(ingDeDataProviderV10.getProviderIdentifier(), ingDeDataProviderV10, ZoneId.of("Europe/Berlin")),
                arguments(ingDeDataProviderV11.getProviderIdentifier(), ingDeDataProviderV11, ZoneId.of("Europe/Berlin"))
        );
    }

    @ParameterizedTest
    @MethodSource("getIngDataProviders")
    public void shouldReturnDataForFetchDataWithMissingData(String identifier,
                                                            IngDataProviderV9 subject,
                                                            ZoneId expectedZone) throws ProviderFetchDataException, JsonProcessingException, TokenInvalidException {
        // given
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(UUID.randomUUID(),
                mapper.writeValueAsString(prepareProperUserAccessMeans()),
                Date.from(ZonedDateTime.now(expectedZone).plusDays(7).toInstant()),
                Date.from(ZonedDateTime.now(expectedZone).plusDays(14).toInstant()));
        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Clock.systemUTC().instant())
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();

        // when
        DataProviderResponse response = subject.fetchData(urlFetchDataRequest);

        // then
        assertThat(response.getAccounts().size()).isEqualTo(1);
        ProviderAccountDTO account = response.getAccounts().get(0);
        assertThat(account.getCurrency()).isNull();
        assertThat(account.getAvailableBalance()).isNull();
        assertThat(account.getCurrentBalance()).isNull();
        assertThat(account.getTransactions().size()).isEqualTo(2);
        assertThat(account.getTransactions().get(0).getAmount()).isEqualTo("100.12");
        assertThat(account.getTransactions().get(0).getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(account.getTransactions().get(0).getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(account.getTransactions().get(0).getDateTime().getZone()).isEqualTo(expectedZone);
        assertThat(account.getTransactions().get(0).getDateTime().getZone()).isEqualTo(expectedZone);
        assertThat(account.getTransactions().get(1).getAmount()).isNull();
        assertThat(account.getTransactions().get(1).getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(account.getTransactions().get(1).getType()).isNull();
        assertThat(account.getTransactions().get(1).getDateTime().getZone()).isEqualTo(expectedZone);
    }

    private String loadPemFile(final String fileName) throws IOException {
        URI uri = resourceLoader.getResource("classpath:certificates/" + fileName).getURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }

    private IngUserAccessMeans prepareProperUserAccessMeans() {
        IngAuthData tokenResponse = prepareProperTokenResponse();
        return new IngUserAccessMeans(tokenResponse, new IngClientAccessMeans(tokenResponse, AUTHENTICATION_MEANS_REFERENCE, Clock.systemUTC()), Clock.systemUTC());
    }

    private IngAuthData prepareProperTokenResponse() {
        TestIngAuthData tokenResponse = new TestIngAuthData();
        tokenResponse.setAccessToken(EXPECTED_ACCESS_TOKEN);
        tokenResponse.setRefreshToken(EXPECTED_REFRESH_TOKEN);
        tokenResponse.setClientId(CLIENT_ID);
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setExpiresIn(0L);
        tokenResponse.setRefreshTokenExpiresIn(0L);
        return tokenResponse;
    }
}
