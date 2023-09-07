package com.yolt.providers.cbiglobe.common;

import com.yolt.providers.cbiglobe.CbiGlobeSampleTypedAuthenticationMeans;
import com.yolt.providers.cbiglobe.SignerMock;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
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

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains flow for scenarios when 500 error occurs during payment initiation process
 * In such case we assume payment is not properly created/initiated and we set REJECTED payment status
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(
        stubs = "classpath:/stubs/pis/3.0/initiate_payment_500",
        httpsPort = 0, port = 0)
@ActiveProfiles("cbiglobe")
public class CbiGlobeSepaPaymentProviderV3InitiatePayment500IntegrationTest {

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    private RestTemplateManager restTemplateManager;

    private Signer signer = new SignerMock();

    @Autowired
    @Qualifier("PosteItalianeSepaPaymentProviderV3")
    private CbiGlobeSepaPaymentProviderV3 posteItalianePaymentProviderV3;

    @Autowired
    @Qualifier("IntesaSanpaoloSepaPaymentProviderV3")
    private CbiGlobeSepaPaymentProviderV3 intesaSanpaoloPaymentProviderV3;

    private Stream<SepaPaymentProvider> getPaymentProviders() {
        return Stream.of(posteItalianePaymentProviderV3, intesaSanpaoloPaymentProviderV3);
    }

    @BeforeEach
    void initialize() {
        authenticationMeans = new CbiGlobeSampleTypedAuthenticationMeans().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    public void shouldReturnResponseWithEmptyLoginUrlAndEmptyProviderStateAndProperStatusesInPecMetadataForInitiatePaymentWhen500Error(SepaPaymentProvider paymentProvider) {
        //given
        var sepaInitiatePaymentRequestDTO = prepareInitPaymentRequest().build();
        var initiatePaymentRequest = buildInitiateRequestBuilder(sepaInitiatePaymentRequestDTO);

        //when
        var result = paymentProvider.initiatePayment(initiatePaymentRequest);

        //then
        assertThat(result.getLoginUrl()).isEmpty();
        assertThat(result.getProviderState()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                                statuses -> statuses.getRawBankPaymentStatus().getReason(),
                                PaymentStatuses::getPaymentStatus)
                        .contains("500",
                                "Generic Error",
                                EnhancedPaymentStatus.INITIATION_ERROR));
    }

    private static SepaInitiatePaymentRequestDTO.SepaInitiatePaymentRequestDTOBuilder prepareInitPaymentRequest() {
        final DynamicFields dynamicFields = new DynamicFields();
        dynamicFields.setCreditorPostalCountry("NL");
        return SepaInitiatePaymentRequestDTO.builder()
                .debtorAccount(SepaAccountDTO.builder()
                        .currency(CurrencyCode.EUR)
                        .iban("IT70R0306948420100000000187")
                        .build())
                .creditorAccount(SepaAccountDTO.builder()
                        .currency(CurrencyCode.EUR)
                        .iban("IT11E03268444900B2860435030")
                        .build())
                .creditorName("Jan Kowalski")
                .instructedAmount(SepaAmountDTO.builder()
                        .amount(new BigDecimal("1.23"))
                        .build())
                .remittanceInformationUnstructured("For beer")
                .dynamicFields(dynamicFields);
    }

    private InitiatePaymentRequest buildInitiateRequestBuilder(final SepaInitiatePaymentRequestDTO sepaInitiatePaymentRequestDTO) {
        return new InitiatePaymentRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setState("fakeState")
                .setSigner(signer)
                .setPsuIpAddress("127.0.0.1")
                .setBaseClientRedirectUrl("https://www.yolt.com/callback/payment")
                .setRequestDTO(sepaInitiatePaymentRequestDTO)
                .build();
    }
}
