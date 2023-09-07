package com.yolt.providers.bunq.common.pis.pec.initiate;

import com.yolt.providers.bunq.common.model.PaymentAmount;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentRequest;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaAmountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
class DefaultInitiatePaymentRequestBodyProviderTest {

    private DefaultInitiatePaymentRequestBodyProvider requestBodyProvider = new DefaultInitiatePaymentRequestBodyProvider();

    private static Stream<Arguments> getIncorrectRequestsWithExceptionMessage() {
        return Stream.of(
                Arguments.of(preparePaymentRequestDto("",
                        "creditorAccount",
                        "creditorName",
                        "description",
                        new BigDecimal("1.23")), "Debtor account is required"),
                Arguments.of(preparePaymentRequestDto("debtorAccount",
                        "",
                        "creditorName",
                        "description",
                        new BigDecimal("1.23")), "Creditor name and account are required"),
                Arguments.of(preparePaymentRequestDto("debtorAccount",
                        "creditorAccount",
                        "creditorName",
                        "",
                        new BigDecimal("1.23")), "Description is required")
        );

    }

    @Test
    void shouldReturnResponseBodyWhenCorrectDataAreProvided() {
        //given
        var paymentRequest = preparePaymentRequestDto("debtorAccount",
                "creditorAccount",
                "creditorName",
                "description",
                new BigDecimal("1.23"));
        var preExecutionResult = new DefaultInitiatePaymentPreExecutionResult(null, paymentRequest, null, null, null, 0, null, 1L, null);
        var expectedPaymentRequest = PaymentServiceProviderDraftPaymentRequest.builder()
                .senderIban("debtorAccount")
                .counterpartyIban("creditorAccount")
                .counterpartyName("creditorName")
                .description("description")
                .amount(PaymentAmount.builder()
                        .currency("EUR")
                        .value("1.23").build())
                .build();

        //when
        var result = requestBodyProvider.provideHttpRequestBody(preExecutionResult);

        //then
        assertThat(result).isEqualTo(expectedPaymentRequest);
    }

    @ParameterizedTest
    @MethodSource("getIncorrectRequestsWithExceptionMessage")
    void shouldThrowIllegalArgumentExceptionWhenIncorrectDataAreProvider(SepaInitiatePaymentRequestDTO paymentRequestDTO, String expectedExceptionMessage) {
        //given
        var preExecutionResult = new DefaultInitiatePaymentPreExecutionResult(null, paymentRequestDTO, null, null, null, 0, null, 1L, null);

        //when
        ThrowableAssert.ThrowingCallable call = () -> requestBodyProvider.provideHttpRequestBody(preExecutionResult);

        //then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(call)
                .withMessage(expectedExceptionMessage);
    }

    private static SepaInitiatePaymentRequestDTO preparePaymentRequestDto(String debtorAccount,
                                                                          String creditorAccount,
                                                                          String creditorName,
                                                                          String description,
                                                                          BigDecimal amount) {
        return SepaInitiatePaymentRequestDTO.builder()
                .debtorAccount(SepaAccountDTO.builder().iban(debtorAccount).build())
                .creditorAccount(SepaAccountDTO.builder().iban(creditorAccount).build())
                .creditorName(creditorName)
                .remittanceInformationUnstructured(description)
                .instructedAmount(SepaAmountDTO.builder().amount(amount).build())
                .build();
    }
}