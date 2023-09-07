package com.yolt.providers.redsys.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseAccountBalances {
    private List<TppMessage> tppMessages;

    private AccountDetails account;

    private List<Balance> balances;
}
