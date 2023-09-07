package com.yolt.providers.redsys.common.service;

import com.yolt.providers.redsys.common.dto.RequestGetConsent;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;

import java.time.LocalDate;

public interface RedsysConsentObjectService {

    RequestGetConsent getConsentObject(LocalDate validUntil, FilledInUserSiteFormValues filledInUserSiteFormValues);
}
