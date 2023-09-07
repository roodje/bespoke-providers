package com.yolt.providers.stet.generic.dto;

import com.yolt.providers.stet.generic.dto.beneficiary.StetBeneficiaryDTO;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TestStetBeneficiaryDTO implements StetBeneficiaryDTO {
    
    private String creditorIban;
    private String creditorOtherIdentification;
    private String creditorOtherSchemeName;
    private String creditorName;
}
