package com.yolt.providers.stet.generic.dto.beneficiary;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface StetBeneficiaryDTO {

    @JsonPath("$.creditorAccount.iban")
    String getCreditorIban();

    @JsonPath("$.creditorAccount.other.identification")
    String getCreditorOtherIdentification();

    @JsonPath("$.creditorAccount.other.schemeName")
    String getCreditorOtherSchemeName();

    @JsonPath("$.creditor.name")
    String getCreditorName();
}