package com.yolt.providers.redsys.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseAccountsList {
    private String psuMessage;

    private TppMessage tppMessage;

    private List<AccountDetails> accounts;
}
