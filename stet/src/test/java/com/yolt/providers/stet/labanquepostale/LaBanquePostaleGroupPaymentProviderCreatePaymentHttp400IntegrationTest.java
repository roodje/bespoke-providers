package com.yolt.providers.stet.labanquepostale;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.stet.generic.GenericPaymentProviderV2;
import com.yolt.providers.stet.generic.GenericPaymentProviderV3;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus.INITIATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/labanquepostale/pis/create-payment-400", httpsPort = 0, port = 0)
@ActiveProfiles("labanquepostale")
class LaBanquePostaleGroupPaymentProviderCreatePaymentHttp400IntegrationTest {

    @Autowired
    private Signer signer;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("LaBanquePostalePaymentProviderV2")
    private GenericPaymentProviderV3 laBanquePostalePaymentProvider;

    private Stream<GenericPaymentProviderV3> getPaymentProviders() {
        return Stream.of(laBanquePostalePaymentProvider);
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldThrowCreationFailedExceptionWhenHttp400IsReceived(GenericPaymentProviderV3 paymentProvider) {
        // given
        DynamicFields dynamicFields = new DynamicFields();
        dynamicFields.setCreditorAgentBic("Creditor Agent Bic");
        dynamicFields.setCreditorAgentName("ING Bank N.V.");

        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .executionDate(LocalDate.now())
                .dynamicFields(dynamicFields)
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "FR7620041010011408742123455"))
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "FR7620041010011408742123456"))
                .creditorName("TEST CREDITOR NAME")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("4.00")))
                .remittanceInformationUnstructured("TEST REMITTANCE INFORMATION")
                .build();

        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(LaBanquePostaleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setBaseClientRedirectUrl("https://www.yolt.com/callback-acc/payment")
                .setState("TEST_STATE")
                .build();

        // when
        LoginUrlAndStateDTO result = paymentProvider.initiatePayment(initiatePaymentRequest);

        // then
        assertThat(result.getLoginUrl()).isEmpty();
        assertThat(result.getProviderState()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata())
                .satisfies(pecMetadata -> assertThat(pecMetadata.getPaymentStatuses())
                        .extracting(
                                statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                                statuses -> statuses.getRawBankPaymentStatus().getReason(),
                                PaymentStatuses::getPaymentStatus)
                        .contains("Bad Request", "Something went wrong", INITIATION_ERROR));
    }
}
