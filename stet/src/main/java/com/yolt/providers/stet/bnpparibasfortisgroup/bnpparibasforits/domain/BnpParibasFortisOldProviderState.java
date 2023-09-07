package com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.domain;

import lombok.Data;

@Data
public class BnpParibasFortisOldProviderState {

    private String accessToken;
    private String scope;
    private String tokenType;
    private long expiresIn;
}
