package com.yolt.providers.stet.generic.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class StetSupplementaryData {

    private String successfulReportUrl;
    private String unsuccessfulReportUrl;
}