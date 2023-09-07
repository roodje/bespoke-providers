package com.yolt.providers.openbanking.ais.hsbcgroup.common.service.pis;

import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduled2DataInitiation;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class HsbcGroupScheduledPaymentRequestValidator implements PaymentRequestValidator<OBWriteDomesticScheduled2DataInitiation> {

    private final Clock clock;

    private static final Pattern REFERENCE_REGEX = Pattern.compile("^[A-Za-z0-9\\/\\s\\.\\+\\:\\(,\\&\\)-?]{1,18}$");
    private static final String SORT_CODE_ACCOUNT_NUMBER = "UK.OBIE.SortCodeAccountNumber";
    private static final String CURRENCY = CurrencyCode.GBP.toString();

    @Override
    public void validateRequest(OBWriteDomesticScheduled2DataInitiation dataInitiation) {
        var reference = dataInitiation.getRemittanceInformation().getReference();
        if (reference == null || !REFERENCE_REGEX.matcher(reference).matches()) {
            throw new IllegalArgumentException("Remittance information contains not allowed characters. It should match " + REFERENCE_REGEX);
        }

        var requestExecutionDate = dataInitiation.getRequestedExecutionDateTime();
        if (ObjectUtils.isEmpty(requestExecutionDate)) {
            throw new IllegalArgumentException("Execution date is required");
        }

        if (requestExecutionDate.isBefore(OffsetDateTime.now(clock))) {
            throw new IllegalArgumentException("Execution date for scheduled payment can't be in past");
        }
        if (requestExecutionDate.isAfter(OffsetDateTime.now(clock).plusYears(1))) {
            throw new IllegalArgumentException("Execution date for scheduled payment can't be more than one year ahead");
        }

        if (ObjectUtils.isNotEmpty(dataInitiation.getDebtorAccount()) &&
                !SORT_CODE_ACCOUNT_NUMBER.equals(dataInitiation.getDebtorAccount().getSchemeName())) {
            throw new IllegalArgumentException("Only UK.OBIE.SortCodeAccountNumber is supported as debtor scheme");
        }

        var creditorScheme = dataInitiation.getCreditorAccount().getSchemeName();
        if (!SORT_CODE_ACCOUNT_NUMBER.equals(creditorScheme)) {
            throw new IllegalArgumentException("Only UK.OBIE.SortCodeAccountNumber is supported as creditor scheme");
        }

        var instructedAmount = dataInitiation.getInstructedAmount();
        if (StringUtils.isEmpty(instructedAmount.getAmount())) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (!CURRENCY.equals(instructedAmount.getCurrency())) {
            throw new IllegalArgumentException("Only GBP is supported currency");
        }

    }
}
