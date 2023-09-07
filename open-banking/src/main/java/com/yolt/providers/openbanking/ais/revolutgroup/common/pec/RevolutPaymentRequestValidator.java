package com.yolt.providers.openbanking.ais.revolutgroup.common.pec;

import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationCreditorAccount;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationDebtorAccount;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class RevolutPaymentRequestValidator implements PaymentRequestValidator<OBWriteDomestic2DataInitiation> {

    private static final Pattern AMOUNT_REGEX = Pattern.compile("^\\d{1,13}$|^\\d{1,13}\\.\\d{1,5}$");
    private static final Pattern INSTRUCTION_IDENTIFICATION_REGEX = Pattern.compile("^.{1,35}$");
    private static final Pattern E2E_IDENTIFICATION_REGEX = Pattern.compile("^.{1,35}$");
    private static final Pattern CREDITOR_NAME_REGEX = Pattern.compile("^.{1,70}$");
    private static final Pattern REMITTANCE_INFO_UNSTRUCTURED_REGEX = Pattern.compile("^.{1,140}$");
    private static final Pattern ACCOUNT_IDENTIFICATION_REGEX = Pattern.compile("^\\S{1,256}$");

    @Override
    public void validateRequest(OBWriteDomestic2DataInitiation dataInitiation) {
        validateAmount(dataInitiation.getInstructedAmount().getAmount());
        validateInstructionIdentification(dataInitiation.getInstructionIdentification());
        validateEndToEndIdentification(dataInitiation.getEndToEndIdentification());
        validateCreditorAccount(dataInitiation.getCreditorAccount());
        validateDebtorAccount(dataInitiation.getDebtorAccount());
        validateRemittanceInfoUnstructured(dataInitiation.getRemittanceInformation().getUnstructured());
    }

    private void validateDebtorAccount(OBWriteDomestic2DataInitiationDebtorAccount debtorAccount) {
        if (debtorAccount == null) {
            return;
        }
        validateAccountName(debtorAccount.getName());
        validateAccountIdentification(debtorAccount.getIdentification());
    }

    private void validateCreditorAccount(OBWriteDomestic2DataInitiationCreditorAccount creditorAccount) {
        validateAccountName(creditorAccount.getName());
        validateAccountIdentification(creditorAccount.getIdentification());
    }

    private void validateAccountIdentification(String identification) {
        if (StringUtils.isBlank(identification) || !ACCOUNT_IDENTIFICATION_REGEX.matcher(identification).matches()) {
            throw new IllegalArgumentException(String.format("Illegal account identification provided. It should match the following pattern: %s", ACCOUNT_IDENTIFICATION_REGEX.toString()));
        }
    }

    private void validateRemittanceInfoUnstructured(String unstructured) {
        if (StringUtils.isNotBlank(unstructured) && !REMITTANCE_INFO_UNSTRUCTURED_REGEX.matcher(unstructured).matches()) {
            throw new IllegalArgumentException(String.format("Illegal remittance info unstructured provided. It should match the following pattern: %s", REMITTANCE_INFO_UNSTRUCTURED_REGEX.toString()));
        }
    }

    private void validateAccountName(String creditorName) {
        if (StringUtils.isBlank(creditorName) || !CREDITOR_NAME_REGEX.matcher(creditorName).matches()) {
            throw new IllegalArgumentException(String.format("Illegal creditor name provided. It should match the following pattern: %s", CREDITOR_NAME_REGEX.toString()));
        }
    }

    private void validateAmount(String amount) {
        if (StringUtils.isBlank(amount) || !AMOUNT_REGEX.matcher(amount).matches()) {
            throw new IllegalArgumentException(String.format("Illegal payment amount provided. It should match the following pattern: %s", AMOUNT_REGEX.toString()));
        }
    }

    private void validateInstructionIdentification(String instructionIdentification) {
        if (StringUtils.isBlank(instructionIdentification) || !INSTRUCTION_IDENTIFICATION_REGEX.matcher(instructionIdentification).matches()) {
            throw new IllegalArgumentException(String.format("Illegal instruction identification provided. It should match the following pattern: %s", INSTRUCTION_IDENTIFICATION_REGEX.toString()));
        }
    }

    private void validateEndToEndIdentification(String e2eIdentifciation) {
        if (StringUtils.isBlank(e2eIdentifciation) || !E2E_IDENTIFICATION_REGEX.matcher(e2eIdentifciation).matches()) {
            throw new IllegalArgumentException(String.format("Illegal end-to-end identification provided. It should match the following pattern: %s", E2E_IDENTIFICATION_REGEX.toString()));
        }
    }
}
