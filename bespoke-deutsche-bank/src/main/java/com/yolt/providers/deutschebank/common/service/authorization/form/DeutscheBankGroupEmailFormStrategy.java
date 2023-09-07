package com.yolt.providers.deutschebank.common.service.authorization.form;

import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.TextField;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.HOURS;

@RequiredArgsConstructor
public class DeutscheBankGroupEmailFormStrategy implements DeutscheBankGroupFormStrategy {

    private static final String EMAIL_FIELD_ID = "email";
    private static final String EMAIL_FIELD_NAME = "E-mail (structure: user.name@domain.com)";
    private static final String EMAIL_REGEX = "^\\S+@\\S+\\.\\S+$";

    private final Clock clock;

    @Override
    public FormStep createForm() {
        TextField emailField = createEmailField();

        Form form = new Form();
        form.setFormComponents(Collections.singletonList(emailField));
        return new FormStep(form, EncryptionDetails.noEncryption(), Instant.now(clock).plus(1L, HOURS), null);
    }

    private TextField createEmailField() {
        return new TextField(EMAIL_FIELD_ID, EMAIL_FIELD_NAME, 0, 255, false, EMAIL_REGEX, false);
    }

    @Override
    public String getPsuId(FilledInUserSiteFormValues formValues) {
        String psuId = formValues.get(EMAIL_FIELD_ID);
        if (Objects.isNull(psuId)) {
            throw new IllegalStateException("Cannot compose PSU-ID based on given Form");
        }

        if (!Pattern.compile(EMAIL_REGEX).matcher(psuId).matches()) {
            throw new IllegalStateException("Invalid e-mail provided");
        }

        return psuId;
    }
}
