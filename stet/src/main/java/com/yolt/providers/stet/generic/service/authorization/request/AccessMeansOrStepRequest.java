package com.yolt.providers.stet.generic.service.authorization.request;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import lombok.Value;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;

import java.util.UUID;

@Value
public class AccessMeansOrStepRequest {

    private String providerState;
    private DefaultAuthenticationMeans authMeans;
    private String redirectUrlPostedBackFromSite;
    private String baseClientRedirectUrl;
    private UUID userId;
    private String state;
    private FilledInUserSiteFormValues filledInUserSiteFormValues;
    private Signer signer;
}
