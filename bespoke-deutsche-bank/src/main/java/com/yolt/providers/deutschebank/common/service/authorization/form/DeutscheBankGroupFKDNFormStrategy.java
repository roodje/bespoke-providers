package com.yolt.providers.deutschebank.common.service.authorization.form;

import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.TextField;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.HOURS;

@RequiredArgsConstructor
public class DeutscheBankGroupFKDNFormStrategy implements DeutscheBankGroupFormStrategy {

    private static final String BRANCH_NUMBER_FIELD_ID = "branch-number";
    private static final String BRANCH_NUMBER_FIELD_NAME = "Branch Number (three-digit)";
    private static final String ACCOUNT_NUMBER_FIELD_ID = "account-number";
    private static final String ACCOUNT_NUMBER_FIELD_NAME = "Account Number (seven-digit, without sub-account number)";

    private final Clock clock;

    @Override
    public FormStep createForm() {
        TextField branchField = createTextField(BRANCH_NUMBER_FIELD_ID, BRANCH_NUMBER_FIELD_NAME, 3);
        TextField accountField = createTextField(ACCOUNT_NUMBER_FIELD_ID, ACCOUNT_NUMBER_FIELD_NAME, 7);

        Form form = new Form();
        form.setFormComponents(Arrays.asList(branchField, accountField));
        return new FormStep(form, EncryptionDetails.noEncryption(), Instant.now(clock).plus(1L, HOURS), null);
    }

    private TextField createTextField(String id, String displayName, int maxLength) {
        return new TextField(id, displayName, 0, maxLength, false, false);
    }

    @Override
    public String getPsuId(FilledInUserSiteFormValues formValues) {
        String branchNumber = formValues.get(BRANCH_NUMBER_FIELD_ID);
        String accountNumber = formValues.get(ACCOUNT_NUMBER_FIELD_ID);
        if (Objects.isNull(branchNumber) || Objects.isNull(accountNumber)) {
            throw new IllegalStateException("Cannot compose PSU-ID based on given Form");
        }
        return branchNumber + accountNumber;
    }
}
