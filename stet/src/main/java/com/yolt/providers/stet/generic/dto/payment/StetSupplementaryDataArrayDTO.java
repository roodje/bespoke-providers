package com.yolt.providers.stet.generic.dto.payment;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class StetSupplementaryDataArrayDTO extends StetSupplementaryData {

    @Builder
    public StetSupplementaryDataArrayDTO(String successfulReportUrl, String unsuccessfulReportUrl, List<StetAuthenticationApproach> acceptedAuthenticationApproach, List<StetAuthenticationApproach> appliedAuthenticationApproach) {
        super(successfulReportUrl, unsuccessfulReportUrl);
        this.acceptedAuthenticationApproach = acceptedAuthenticationApproach;
        this.appliedAuthenticationApproach = appliedAuthenticationApproach;
    }

    private List<StetAuthenticationApproach> acceptedAuthenticationApproach;
    private List<StetAuthenticationApproach> appliedAuthenticationApproach;

}