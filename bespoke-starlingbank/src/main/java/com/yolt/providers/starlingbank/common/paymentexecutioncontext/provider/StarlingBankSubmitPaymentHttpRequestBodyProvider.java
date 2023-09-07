package com.yolt.providers.starlingbank.common.paymentexecutioncontext.provider;

import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.starlingbank.common.model.*;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult;

import java.math.BigDecimal;
import java.util.UUID;

import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER;

public class StarlingBankSubmitPaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult, PaymentRequest> {

    private static final String REMITTANCE_INFORMATION_STRUCTURED = "remittanceInformationStructured";

    @Override
    public PaymentRequest provideHttpRequestBody(StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult preExecutionResult) throws PaymentExecutionTechnicalException {
        InitiateUkDomesticPaymentRequestDTO paymentRequest = preExecutionResult.getPaymentRequest();
        UkAccountDTO creditorAccount = paymentRequest.getCreditorAccount();
        String remittanceReference = getRemittanceReference(paymentRequest);
        return createPaymentBody(
                UUID.randomUUID().toString(),
                creditorAccount.getAccountHolderName(),
                creditorAccount.getAccountIdentifier(),
                creditorAccount.getAccountIdentifierScheme().name(),
                remittanceReference != null ? remittanceReference : paymentRequest.getRemittanceInformationUnstructured(),
                paymentRequest.getCurrencyCode(),
                paymentRequest.getAmount());
    }

    private PaymentRequest createPaymentBody(String externalPaymentId,
                                             String creditorHolderName,
                                             String sortCodeAndAccountNumber,
                                             String scheme,
                                             String remittanceReference,
                                             String currencyCode,
                                             BigDecimal amount) {
        return PaymentRequest.builder()
                .externalIdentifier(externalPaymentId)
                .paymentRecipient(getPaymentRecipient(creditorHolderName, sortCodeAndAccountNumber, scheme))
                .reference(remittanceReference)
                .amount(new CurrencyAndAmountV2(currencyCode, amount.multiply(BigDecimal.valueOf(100L)).setScale(0)))
                .build();

    }

    private PaymentRecipient getPaymentRecipient(String holderName,
                                                 String sortCodeAndAccountNumber,
                                                 String scheme) {
        return PaymentRecipient.builder()
                .payeeName(holderName)
                .payeeType(PayeeType.INDIVIDUAL)
                .countryCode("GB")
                .accountIdentifier(sortCodeAndAccountNumber.substring(6))
                .bankIdentifier(sortCodeAndAccountNumber.substring(0, 6))
                .bankIdentifierType(mapIdentifierType(scheme))
                .build();

    }

    private BankIdentifierType mapIdentifierType(String scheme) {
        if (SORTCODEACCOUNTNUMBER.name().equals(scheme)) {
            return BankIdentifierType.SORT_CODE;
        }
        throw new IllegalArgumentException("Invalid scheme type.");
    }

    private static String getRemittanceReference(InitiateUkDomesticPaymentRequestDTO payment) {
        return payment.getDynamicFields() != null ? payment.getDynamicFields().get(REMITTANCE_INFORMATION_STRUCTURED) : null;
    }
}
