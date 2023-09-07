package com.yolt.providers.redsys.common.dto;

import lombok.Data;

@Data
public class Links {
    private LinkReference scaRedirect;

    private LinkReference startAuthorisationWithAuthenticationMethodSelection;

    private LinkReference scaStatus;

    private LinkReference startAuthorisation;

    private LinkReference self;

    private LinkReference status;
}
