package com.yolt.providers.openbanking.ais.permanenttsbgroup.common.autoonboarding;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PermanentTsbGroupRegistrationClaims {

    ORGANISATION_NAME("Organisation_Name"),
    APPLICATION_NAME("Application_Name"),
    REDIRECT_URI("Redirect_URI"),
    BUSINESS_CONTACT_NAME("Business_Contact_Name"),
    BUSINESS_CONTACT_EMAIL("Business_Contact_Email"),
    BUSINESS_CONTACT_PHONE("Business_Contact_Phone"),
    TECHNICAL_CONTACT_NAME("Technical_Contact_Name"),
    TECHNICAL_CONTACT_EMAIL("Technical_Contact_Email"),
    TECHNICAL_CONTACT_PHONE("Technical_Contact_Phone");

    private String value;
}
