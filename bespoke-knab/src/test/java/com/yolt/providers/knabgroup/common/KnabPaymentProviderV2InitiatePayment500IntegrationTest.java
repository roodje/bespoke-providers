package com.yolt.providers.knabgroup.common;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.knabgroup.TestRestTemplateManager;
import com.yolt.providers.knabgroup.TestSigner;
import com.yolt.providers.knabgroup.samples.SampleAuthenticationMeans;
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

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains flow for scenarios when 500 error
 * occurs during payment initiation process
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/pis/initiate-payment-http500", httpsPort = 0, port = 0)
public class KnabPaymentProviderV2InitiatePayment500IntegrationTest {

    private TestRestTemplateManager restTemplateManager;
    private Signer signer;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Autowired
    @Qualifier("KnabPaymentProvider")
    private KnabGroupPaymentProvider knabPaymentProvider;

    private static final String STATE = "66a32124-b334-4eb8-8700-d6ca9e4410a0";

    Stream<SepaPaymentProvider> getPaymentProviders() {
        return Stream.of(knabPaymentProvider);
    }

    @SneakyThrows
    @BeforeEach
    public void setup() {
        authenticationMeans = SampleAuthenticationMeans.getSampleAuthenticationMeans();
        restTemplateManager = new TestRestTemplateManager(externalRestTemplateBuilderFactory);
        signer = new TestSigner();
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    public void shouldReturnLoginUrlAndStateDTOWithEmptyLoginUrlAndEmptyStateAndPecMetadataWithProperStatusesForInitiatePaymentWhenInternalServerError(final SepaPaymentProvider subject) {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL91ABNA0417164300"))
                .creditorName("Jonas Snow")
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL52KNAB9992936932"))
                .endToEndIdentification("123456789012345")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("10.00")))
                .remittanceInformationUnstructured("Utility bill")
                .build();
        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO)
                .setBaseClientRedirectUrl("https://www.yoltTestUrl.com")
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setState(STATE)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("127.0.0.1")
                .build();

        // when
        LoginUrlAndStateDTO result = subject.initiatePayment(initiatePaymentRequest);

        // then
        assertThat(result.getProviderState()).isEmpty();
        assertThat(result.getLoginUrl()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                                statuses -> statuses.getRawBankPaymentStatus().getReason(),
                                PaymentStatuses::getPaymentStatus)
                        .contains("PAYMENT_FAILED", "", EnhancedPaymentStatus.INITIATION_ERROR));
    }
}
