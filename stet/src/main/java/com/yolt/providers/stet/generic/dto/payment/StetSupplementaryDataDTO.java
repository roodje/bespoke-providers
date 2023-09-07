package com.yolt.providers.stet.generic.dto.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
public class StetSupplementaryDataDTO extends StetSupplementaryData {

    @Builder
    public StetSupplementaryDataDTO(String successfulReportUrl, String unsuccessfulReportUrl, StetAuthenticationApproach acceptedAuthenticationApproach, StetAuthenticationApproach appliedAuthenticationApproach) {
        super(successfulReportUrl, unsuccessfulReportUrl);
        this.acceptedAuthenticationApproach = acceptedAuthenticationApproach;
        this.appliedAuthenticationApproach = appliedAuthenticationApproach;
    }

    private StetAuthenticationApproach acceptedAuthenticationApproach;
    private StetAuthenticationApproach appliedAuthenticationApproach;
}