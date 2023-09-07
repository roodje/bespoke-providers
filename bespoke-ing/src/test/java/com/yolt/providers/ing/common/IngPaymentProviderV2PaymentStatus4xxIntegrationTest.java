package com.yolt.providers.ing.common;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.pis.sepa.GetStatusRequestBuilder;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.common.util.SimpleRestTemplateManagerMock;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains flow for scenarios when 4xx error
 * occurs during get payment status process
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/submit-payment-http4xx", httpsPort = 0, port = 0)
public class IngPaymentProviderV2PaymentStatus4xxIntegrationTest {

    private RestTemplateManager restTemplateManager;
    private Signer signer;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("IngFrPaymentProviderV2")
    private IngPaymentProviderV2 ingFrPaymentProvider;

    @Autowired
    @Qualifier("IngItPaymentProviderV2")
    private IngPaymentProviderV2 ingItPaymentProvider;

    @Autowired
    @Qualifier("IngNlPaymentProviderV3")
    private IngPaymentProviderV3 ingNlPaymentProvider;

    @Autowired
    @Qualifier("IngRoPaymentProviderV2")
    private IngPaymentProviderV2 ingRoPaymentProvider;

    Stream<SepaPaymentProvider> getIngPaymentProviders() {
        return Stream.of(ingFrPaymentProvider, ingItPaymentProvider, ingNlPaymentProvider, ingRoPaymentProvider);
    }

    @SneakyThrows
    @BeforeEach
    public void setup() {
        authenticationMeans = new IngSampleAuthenticationMeans().getAuthenticationMeans();

        restTemplateManager = new SimpleRestTemplateManagerMock(externalRestTemplateBuilderFactory);

        PrivateKey signingKey = KeyUtil.createPrivateKeyFromPemFormat((loadPemFile("example_client_signing.key")));
        signer = new TestSigner(signingKey);
    }

    @ParameterizedTest
    @MethodSource("getIngPaymentProviders")
    public void shouldReturnSepaPaymentStatusResponseDTOWithEmptyPaymentIdAndPecMetadataWithProperStatusesForGetPaymentStatusWhen4xxError(final SepaPaymentProvider sut) {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setPaymentId("cc04ef71-085e-468e-87bd-2881d8a03a5d")
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("127.0.0.1")
                .build();

        //when
        SepaPaymentStatusResponseDTO result = sut.getStatus(getStatusRequest);

        // then
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("PAYMENT_FAILED");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.UNKNOWN);
                }));
    }

    private String loadPemFile(final String fileName) throws IOException {
        URI uri = resourceLoader.getResource("classpath:certificates/" + fileName).getURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
