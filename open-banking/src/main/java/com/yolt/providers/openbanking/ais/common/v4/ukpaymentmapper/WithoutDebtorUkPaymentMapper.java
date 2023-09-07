package com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper;

import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.UkSchemeMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.*;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@AllArgsConstructor
public class WithoutDebtorUkPaymentMapper implements UkPaymentMapper {

    public static final String REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME = "remittanceInformationStructured";
    private static final int MAX_SIZE_INSTRUCTION_IDENTIFICATION = 30;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private final UkRemittanceInformationMapper remittanceInformationMapper;
    private final UkSchemeMapper schemeMapper;
    private final Clock clock;

    private static OBRisk1 createRisk() {
        OBRisk1 risk = new OBRisk1();
        risk.setPaymentContextCode(OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY);
        return risk;
    }

    private static OBWriteDomestic2DataInitiationInstructedAmount createPaymentDetails(final InitiateUkDomesticPaymentRequestDTO payment) {
        return new OBWriteDomestic2DataInitiationInstructedAmount()
                .amount(formatAmount(payment.getAmount()))
                .currency(payment.getCurrencyCode());
    }

    private static String formatAmount(final BigDecimal amount) {
        int minScale = (amount.scale() > 0 && amount.scale() < 5) ? amount.scale() : 1;
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.UK);
        numberFormat.setGroupingUsed(false);
        numberFormat.setMinimumFractionDigits(minScale);
        numberFormat.setMaximumFractionDigits(5);
        return numberFormat.format(amount);
    }

    @Override
    public OBWriteDomesticConsent4 mapToSetupRequest(InitiateUkDomesticPaymentRequest request) {
        return new OBWriteDomesticConsent4()
                .risk(createRisk())
                .data(new OBWriteDomesticConsent4Data()
                        .initiation(mapToInitiation(request.getRequestDTO())));
    }

    private OBWriteDomestic2DataInitiation mapToInitiation(final InitiateUkDomesticPaymentRequestDTO payment) {
        OBWriteDomestic2DataInitiationCreditorAccount creditorAccount = createCreditorAccount(payment.getCreditorAccount());
        OBWriteDomestic2DataInitiationRemittanceInformation remittanceInformation =
                remittanceInformationMapper.createRemittanceInformation(getRemittanceReference(payment), payment.getRemittanceInformationUnstructured());
        OBWriteDomestic2DataInitiationInstructedAmount paymentDetails = createPaymentDetails(payment);

        return new OBWriteDomestic2DataInitiation()
                .creditorAccount(creditorAccount)
                .endToEndIdentification(payment.getEndToEndIdentification())
                .instructionIdentification(createInstructionIdentification())
                .instructedAmount(paymentDetails)
                .remittanceInformation(remittanceInformation);
    }

    private String createInstructionIdentification() {
        String currentDateTime = LocalDateTime.now(clock).format(DATE_TIME_FORMATTER);
        return currentDateTime + "-" + UUID.randomUUID().toString().substring(0, MAX_SIZE_INSTRUCTION_IDENTIFICATION - currentDateTime.length() - 1);
    }

    private OBWriteDomestic2DataInitiationCreditorAccount createCreditorAccount(final UkAccountDTO creditorAccount) {
        return new OBWriteDomestic2DataInitiationCreditorAccount()
                .schemeName(schemeMapper.map(creditorAccount.getAccountIdentifierScheme()))
                .identification(creditorAccount.getAccountIdentifier())
                .name(creditorAccount.getAccountHolderName())
                .secondaryIdentification(creditorAccount.getSecondaryIdentification());
    }

    private String getRemittanceReference(InitiateUkDomesticPaymentRequestDTO payment) {
        return payment.getDynamicFields() != null ? payment.getDynamicFields().get(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME) : null;
    }

    @Override
    public OBWriteDomestic2 mapToSubmitRequest(String consentId, OBWriteDomestic2DataInitiation paymentIntent) {
        return new OBWriteDomestic2()
                .risk(createRisk())
                .data(new OBWriteDomestic2Data()
                        .consentId(consentId)
                        .initiation(paymentIntent));
    }
}
