package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single.scheduled;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InstructionIdentificationProvider;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.springframework.util.StringUtils.isEmpty;

@RequiredArgsConstructor
public class YoltBankUkInitiateScheduledPaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<YoltBankUkInitiateScheduledPaymentPreExecutionResult, OBWriteDomesticScheduledConsent1> {

    private static final String OB_3_0_0_SCHEME_PREFIX = "UK.OBIE.";
    private static final String REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME = "remittanceInformationStructured";
    private final InstructionIdentificationProvider instructionIdentificationProvider;

    public OBWriteDomesticScheduledConsent1 provideHttpRequestBody(YoltBankUkInitiateScheduledPaymentPreExecutionResult result) {
        return new OBWriteDomesticScheduledConsent1()
                .risk(new OBRisk1()
                        .paymentContextCode(OBExternalPaymentContext1Code.PARTYTOPARTY))
                .data(new OBWriteDataDomesticScheduledConsent1()
                        .initiation(mapToOpenBankingPayment(result.getRequestDTO())));
    }

    private OBDomesticScheduled1 mapToOpenBankingPayment(final InitiateUkDomesticScheduledPaymentRequestDTO payment) {
        OBCashAccountCreditor2 creditorAccount = createCreditorAccount(payment.getCreditorAccount());
        OBCashAccountDebtor3 debtorAccount = createDebtorAccount(payment.getDebtorAccount());
        OBRemittanceInformation1 remittanceInformation =
                createRemittanceInformation(getRemittanceReference(payment), payment.getRemittanceInformationUnstructured());
        OBDomestic1InstructedAmount paymentDetails = createPaymentDetails(payment);
        OffsetDateTime requestedExecutionDateTime = payment.getExecutionDate();

        return new OBDomesticScheduled1()
                .creditorAccount(creditorAccount)
                .debtorAccount(debtorAccount)
                .endToEndIdentification(payment.getEndToEndIdentification())
                .instructionIdentification(instructionIdentificationProvider.getInstructionIdentification())
                .instructedAmount(paymentDetails)
                .remittanceInformation(remittanceInformation)
                .requestedExecutionDateTime(requestedExecutionDateTime);
    }

    private static OBDomestic1InstructedAmount createPaymentDetails(final InitiateUkDomesticPaymentRequestDTO payment) {
        return new OBDomestic1InstructedAmount()
                .amount(payment.getAmount().toPlainString())
                .currency(payment.getCurrencyCode());
    }

    private static OBCashAccountCreditor2 createCreditorAccount(final UkAccountDTO creditorAccount) {
        return new OBCashAccountCreditor2()
                .schemeName(OB_3_0_0_SCHEME_PREFIX + creditorAccount.getAccountIdentifierScheme().toString())
                .identification(creditorAccount.getAccountIdentifier())
                .name(creditorAccount.getAccountHolderName())
                .secondaryIdentification(creditorAccount.getSecondaryIdentification());
    }

    private static OBCashAccountDebtor3 createDebtorAccount(final UkAccountDTO debtorAccount) {
        if (debtorAccount == null || debtorAccount.getAccountIdentifier() == null) {
            return null;
        }
        return new OBCashAccountDebtor3()
                .schemeName(OB_3_0_0_SCHEME_PREFIX + debtorAccount.getAccountIdentifierScheme().toString())
                .identification(debtorAccount.getAccountIdentifier())
                .name(debtorAccount.getAccountHolderName())
                .secondaryIdentification(debtorAccount.getSecondaryIdentification());
    }

    private static OBRemittanceInformation1 createRemittanceInformation(final String remittanceStructured, final String remittanceUnstructured) {
        if (isEmpty(remittanceStructured) && isEmpty(remittanceUnstructured)) {
            return null;
        }
        return new OBRemittanceInformation1()
                .reference(remittanceStructured)
                .unstructured(remittanceUnstructured);
    }

    private static String getRemittanceReference(InitiateUkDomesticPaymentRequestDTO payment) {
        return payment.getDynamicFields() != null ? payment.getDynamicFields().get(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME) : null;
    }
}
