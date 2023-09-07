package com.yolt.providers.volksbank.common.pis;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequestBuilder;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaAmountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.volksbank.FakeRestTemplateManager;
import com.yolt.providers.volksbank.VolksbankSampleTypedAuthenticationMeans;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains flow for scenarios when 500 error
 * occurs during payment initiation process
 * In such case we assume payment is not properly created/initiated and we throw {@link CreationFailedException}.
 * Tests are parametrized and run for all {@link VolksbankSepaPaymentProviderV3} providers in group.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("volksbank")
@AutoConfigureWireMock(httpsPort = 0, port = 0, stubs = {"classpath:/stubs/volksbank/api_1.1/pis/initiate_payment_500"})
public class VolksbankSepaPaymentProviderV3InitiatePayment500IntegrationTest {

    @Autowired
    @Qualifier("ASNBankSepaPaymentProviderV3")
    private VolksbankSepaPaymentProviderV3 asnProviderV3;

    @Autowired
    @Qualifier("SNSBankSepaPaymentProviderV3")
    private VolksbankSepaPaymentProviderV3 snsProviderV3;

    @Autowired
    @Qualifier("RegioBankSepaPaymentProviderV3")
    private VolksbankSepaPaymentProviderV3 regioProviderV3;

    Stream<SepaPaymentProvider> getVolksbankProviders() {
        return Stream.of(regioProviderV3, snsProviderV3, asnProviderV3);
    }

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private RestTemplateManager restTemplateManager;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
        authenticationMeans = new VolksbankSampleTypedAuthenticationMeans().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getVolksbankProviders")
    public void shouldThrowCreationFailedExceptionForInitiatePaymentWhen500Error(SepaPaymentProvider paymentProviderUnderTest) {
        // given
        var sepaInitiatePaymentRequestDTO = SepaInitiatePaymentRequestDTO.builder()
                .debtorAccount(SepaAccountDTO.builder()
                        .currency(CurrencyCode.EUR)
                        .iban("NL64MAART0948305290")
                        .build())
                .creditorAccount(SepaAccountDTO.builder()
                        .currency(CurrencyCode.EUR)
                        .iban("NL55WIND0000012345")
                        .build())
                .creditorName("John Doe")
                .instructedAmount(SepaAmountDTO.builder()
                        .amount(new BigDecimal("123.50"))
                        .build())
                .remittanceInformationUnstructured("payment for 11 currant buns")
                .build();
        var initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setState("state")
                .setPsuIpAddress("127.0.0.1")
                .setBaseClientRedirectUrl("https://www.yolt.com/callback/payment")
                .setRequestDTO(sepaInitiatePaymentRequestDTO)
                .build();

        // when
        var result = paymentProviderUnderTest.initiatePayment(initiatePaymentRequest);

        // then
        assertThat(result.getLoginUrl()).isEmpty();
        assertThat(result.getProviderState()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata -> {
            assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                            statuses -> statuses.getRawBankPaymentStatus().getReason(),
                            PaymentStatuses::getPaymentStatus)
                    .contains("INTERNAL_SERVER_ERROR", "An internal server error occurred.", EnhancedPaymentStatus.INITIATION_ERROR);
        });
    }
}
