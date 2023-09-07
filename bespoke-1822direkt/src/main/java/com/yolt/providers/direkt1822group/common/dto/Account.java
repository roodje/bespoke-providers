package com.yolt.providers.direkt1822group.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    private String resourceId;
    private String iban;
    private String currency;
    private String name;
    private String product;
    private String cashAccountType;
    private String bic;
    private String ownerName;

    @JsonProperty("_links")
    private Links links;
}
