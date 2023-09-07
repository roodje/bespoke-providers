package com.yolt.providers.commerzbankgroup.common.api.dto.authorization;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Consents {

    @JsonProperty("access")
    private AccountAccessEnum access;

    @JsonProperty("recurringIndicator")
    private Boolean recurringIndicator;

    @JsonProperty("frequencyPerDay")
    private Integer frequencyPerDay;

    @JsonProperty("combinedServiceIndicator")
    private Boolean combinedServiceIndicator;

    @JsonProperty("validUntil")
    private String validUntil;

    public Consents access(AccountAccessEnum access) {
        this.access = access;
        return this;
    }

    public Consents recurringIndicator(Boolean recurringIndicator) {
        this.recurringIndicator = recurringIndicator;
        return this;
    }

    public Consents validUntil(String validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    public Consents frequencyPerDay(Integer frequencyPerDay) {
        this.frequencyPerDay = frequencyPerDay;
        return this;
    }

    public Consents combinedServiceIndicator(Boolean combinedServiceIndicator) {
        this.combinedServiceIndicator = combinedServiceIndicator;
        return this;
    }
}

