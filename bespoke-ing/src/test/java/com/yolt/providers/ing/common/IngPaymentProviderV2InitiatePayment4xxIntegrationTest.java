package com.yolt.providers.ing.common;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.common.util.SimpleRestTemplateManagerMock;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
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
import java.math.BigDecimal;
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
 * occurs during payment initiation process
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/initiate-payment-http4xx", httpsPort = 0, port = 0)
public class IngPaymentProviderV2InitiatePayment4xxIntegrationTest {

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

    private static final String STATE = "66a32124-b334-4eb8-8700-d6ca9e4410a0";

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
    public void shouldReturnLoginUrlAndStateDTOWithEmptyLoginUrlAndEmptyProviderStateWithProperStatusesInPecMetadataForInitiatePaymentWhen4xxError(final SepaPaymentProvider sut) {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL91ABNA0417164300"))
                .creditorName("Some Name")
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL91ABNA0417164322"))
                .endToEndIdentification("123456789012345")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("5877.78")))
                .remittanceInformationUnstructured("unstructured-information")
                .build();
        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO)
                .setBaseClientRedirectUrl("https://www.yolt.com/callback/payment")
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setState(STATE)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("111.1.111.111")
                .build();

        // when
        LoginUrlAndStateDTO result = sut.initiatePayment(initiatePaymentRequest);

        // then
        assertThat(result.getProviderState()).isEmpty();
        assertThat(result.getLoginUrl()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                                statuses -> statuses.getRawBankPaymentStatus().getReason(),
                                PaymentStatuses::getPaymentStatus)
                        .contains("PAYMENT_FAILED", "", EnhancedPaymentStatus.INITIATION_ERROR));
    }

    private String loadPemFile(final String fileName) throws IOException {
        URI uri = resourceLoader.getResource("classpath:certificates/" + fileName).getURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
