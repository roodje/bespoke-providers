package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.periodic;

import com.yolt.providers.common.pis.common.UkPeriodicPaymentInfo;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPeriodicPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.*;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class YoltBankUkInitiatePeriodicPaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<YoltBankUkInitiatePeriodicPaymentPreExecutionResult, OBWriteDomesticStandingOrderConsent1> {

    private static final String OB_3_0_0_SCHEME_PREFIX = "UK.OBIE.";
    private static final String REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME = "remittanceInformationStructured";
    private static final LocalTime LOCAL_TIME_OF_THE_PAYMENT = LocalTime.NOON;
    private static final ZoneOffset ZONE_OFFSET_OF_THE_PAYMENT = ZoneOffset.UTC;

    @Override
    public OBWriteDomesticStandingOrderConsent1 provideHttpRequestBody(YoltBankUkInitiatePeriodicPaymentPreExecutionResult result) {
        return new OBWriteDomesticStandingOrderConsent1()
                .risk(new OBRisk1()
                        .paymentContextCode(OBExternalPaymentContext1Code.PARTYTOPARTY))
                .data(new OBWriteDataDomesticStandingOrderConsent1()
                        .initiation(mapToOpenBankingPayment(result.getRequestDTO())));
    }

    private OBDomesticStandingOrder1 mapToOpenBankingPayment(final InitiateUkDomesticPeriodicPaymentRequestDTO payment) {
        OBCashAccountCreditor2 creditorAccount = createCreditorAccount(payment.getCreditorAccount());
        OBCashAccountDebtor3 debtorAccount = createDebtorAccount(payment.getDebtorAccount());
        OBDomesticStandingOrder1FirstPaymentAmount firstPaymentDetails = createFirstPaymentDetails(payment);

        UkPeriodicPaymentInfo periodicPaymentInfo = payment.getPeriodicPaymentInfo();
        OffsetDateTime finalDate = periodicPaymentInfo.getEndDate() != null ? OffsetDateTime.of(periodicPaymentInfo.getEndDate(), LOCAL_TIME_OF_THE_PAYMENT, ZONE_OFFSET_OF_THE_PAYMENT) : null;
        return new OBDomesticStandingOrder1()
                .creditorAccount(creditorAccount)
                .debtorAccount(debtorAccount)
                .reference(getRemittanceReference(payment))
                .firstPaymentDateTime(OffsetDateTime.of(periodicPaymentInfo.getStartDate(), LOCAL_TIME_OF_THE_PAYMENT, ZONE_OFFSET_OF_THE_PAYMENT))
                .finalPaymentDateTime(finalDate)
                .firstPaymentAmount(firstPaymentDetails)
                .frequency(periodicPaymentInfo.getFrequency().toString());
    }

    private static OBDomesticStandingOrder1FirstPaymentAmount createFirstPaymentDetails(final InitiateUkDomesticPaymentRequestDTO payment) {
        return new OBDomesticStandingOrder1FirstPaymentAmount()
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

    private static String getRemittanceReference(InitiateUkDomesticPaymentRequestDTO payment) {
        return payment.getDynamicFields() != null ? payment.getDynamicFields().get(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME) : null;
    }
}
