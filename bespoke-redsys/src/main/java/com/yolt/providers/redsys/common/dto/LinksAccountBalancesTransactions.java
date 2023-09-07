package com.yolt.providers.redsys.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinksAccountBalancesTransactions {
    private LinkReference balances;

    private LinkReference transactions;

    private LinkReference account;
}
