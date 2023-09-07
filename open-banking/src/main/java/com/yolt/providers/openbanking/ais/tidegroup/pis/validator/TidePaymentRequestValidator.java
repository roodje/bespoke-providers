package com.yolt.providers.openbanking.ais.tidegroup.pis.validator;

import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationCreditorAccount;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationInstructedAmount;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

public class TidePaymentRequestValidator implements PaymentRequestValidator<OBWriteDomestic2DataInitiation> {

    private static final Pattern INSTRUCTION_IDENTIFICATION_REGEX = Pattern.compile("^[a-zA-Z0-9/?:().,+ #=!-]{1,35}$");
    private static final Pattern E2E_IDENTIFICATION_REGEX = Pattern.compile("^.{1,31}$");
    private static final Pattern AMOUNT_REGEX = Pattern.compile("^\\d{1,13}\\.\\d{1,5}$");
    private static final String ONLY_AVAILABLE_CURRENCY = "GBP";
    private static final Pattern CREDITOR_NAME_REGEX = Pattern.compile("^.{1,70}$");
    private static final Pattern REMITTANCE_INFO_UNSTRUCTURED_REGEX = Pattern.compile("^.{1,140}$");
    private static final Pattern REFERENCE_REGEX = Pattern.compile("^[A-Za-z0-9 &\\-./]{6,35}$");
    private static final String AVAILABLE_SCHEME_NAME = "UK.OBIE.SortCodeAccountNumber";
    private static final Pattern ACCOUNT_IDENTIFICATION_REGEX = Pattern.compile("^\\d{1,256}$");

    @Override
    public void validateRequest(OBWriteDomestic2DataInitiation dataInitiation) {
        validateInstructionIdentification(dataInitiation.getInstructionIdentification());
        validateEndToEndIdentification(dataInitiation.getEndToEndIdentification());
        validateAmount(dataInitiation.getInstructedAmount());
        validateCreditorAccount(dataInitiation.getCreditorAccount());
        validateRemittanceInfoUnstructured(dataInitiation.getRemittanceInformation().getUnstructured());
        validateReference(dataInitiation.getRemittanceInformation().getReference());
    }

    private void validateCreditorAccount(OBWriteDomestic2DataInitiationCreditorAccount creditorAccount) {
        validateAccountName(creditorAccount.getName());
        validateAccountSchemeName(creditorAccount.getSchemeName());
        validateAccountIdentification(creditorAccount.getIdentification());
    }

    private void validateAccountIdentification(String identification) {
        if (StringUtils.isBlank(identification) || !ACCOUNT_IDENTIFICATION_REGEX.matcher(identification).matches()) {
            throw new IllegalArgumentException(String.format("Illegal account identification provided. It should match the following pattern: %s", ACCOUNT_IDENTIFICATION_REGEX.toString()));
        }
    }

    private void validateAccountSchemeName(String scheme) {
        if (StringUtils.isBlank(scheme) || !AVAILABLE_SCHEME_NAME.equals(scheme)) {
            throw new IllegalArgumentException(String.format("Illegal scheme name provided. It should be: %s", AVAILABLE_SCHEME_NAME));
        }
    }

    private void validateRemittanceInfoUnstructured(String unstructured) {
        if (StringUtils.isNotBlank(unstructured) && !REMITTANCE_INFO_UNSTRUCTURED_REGEX.matcher(unstructured).matches()) {
            throw new IllegalArgumentException(String.format("Illegal remittance info unstructured provided. It should match the following pattern: %s", REMITTANCE_INFO_UNSTRUCTURED_REGEX.toString()));
        }
    }

    private void validateReference(String reference) {
        if (StringUtils.isBlank(reference) || !REFERENCE_REGEX.matcher(reference).matches()) {
            throw new IllegalArgumentException(String.format("Illegal remittance info structured provided. It should match the following pattern: %s", REFERENCE_REGEX.toString()));
        }
        if (!reference.matches(".*[a-zA-Z0-9]{6}.*")) {
            throw new IllegalArgumentException("Illegal remittance info structured provided. Must contain a contiguous string of at least 6 alphanumeric characters");
        }
        String alphaNumericReference = reference.replaceAll("[^a-zA-Z0-9]", "");
        char firstCharacter = alphaNumericReference.charAt(0);
        if (alphaNumericReference.chars().allMatch(value -> value == firstCharacter)) {
            throw new IllegalArgumentException("Illegal remittance info structured provided. After stripping out non-alphanumeric characters the resulting string cannot consist of all the same character");
        }
    }

    private void validateAccountName(String creditorName) {
        if (StringUtils.isBlank(creditorName) || !CREDITOR_NAME_REGEX.matcher(creditorName).matches()) {
            throw new IllegalArgumentException(String.format("Illegal creditor name provided. It should match the following pattern: %s", CREDITOR_NAME_REGEX.toString()));
        }
    }

    private void validateAmount(OBWriteDomestic2DataInitiationInstructedAmount amount) {
        if (Objects.isNull(amount) || StringUtils.isBlank(amount.getAmount()) || !AMOUNT_REGEX.matcher(amount.getAmount()).matches()) {
            throw new IllegalArgumentException(String.format("Illegal payment amount provided. It should match the following pattern: %s", AMOUNT_REGEX.toString()));
        }
        if (StringUtils.isBlank(amount.getCurrency()) || !ONLY_AVAILABLE_CURRENCY.equals(amount.getCurrency())) {
            throw new IllegalArgumentException("Illegal currency value provided. It should be set to GBP");
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
