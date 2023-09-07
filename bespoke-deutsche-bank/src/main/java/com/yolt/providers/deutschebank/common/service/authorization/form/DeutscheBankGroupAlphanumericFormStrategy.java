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

import static java.time.temporal.ChronoUnit.HOURS;

@RequiredArgsConstructor
public class DeutscheBankGroupAlphanumericFormStrategy implements DeutscheBankGroupFormStrategy {

    private static final String USERNAME_FIELD_ID = "username";
    private static final String USERNAME_FIELD_NAME = "Username (self-defined or unique 2 to 59 alphanumeric characters)";

    private final Clock clock;

    @Override
    public FormStep createForm() {
        TextField usernameField = createUsernameField();

        Form form = new Form();
        form.setFormComponents(Collections.singletonList(usernameField));
        return new FormStep(form, EncryptionDetails.noEncryption(), Instant.now(clock).plus(1L, HOURS), null);
    }

    private TextField createUsernameField() {
        return new TextField(USERNAME_FIELD_ID, USERNAME_FIELD_NAME, 0, 255, false, false);
    }

    @Override
    public String getPsuId(FilledInUserSiteFormValues formValues) {
        String psuId = formValues.get(USERNAME_FIELD_ID);
        if (Objects.isNull(psuId)) {
            throw new IllegalStateException("Cannot compose PSU-ID based on given Form");
        }
        return psuId;
    }
}
