package com.yolt.providers.rabobank.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Access {

    @JsonProperty("ais.balances.read")
    private List<AccountConsentData> balancesRead;

    @JsonProperty("ais.transactions.read-90days")
    private List<AccountConsentData> transactionsRead;

    @JsonProperty("ais.transactions.read-history")
    private List<AccountConsentData> transactionsHistoryRead;
}
