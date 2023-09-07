package com.yolt.providers.deutschebank.common.service.authorization.form;

import com.yolt.providers.common.domain.dynamic.step.FormStep;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;

public interface DeutscheBankGroupFormStrategy {

    FormStep createForm();

    String getPsuId(FilledInUserSiteFormValues formValues);
}
