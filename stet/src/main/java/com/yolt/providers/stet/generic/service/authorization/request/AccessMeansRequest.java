package com.yolt.providers.stet.generic.service.authorization.request;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import lombok.Value;
import nl.ing.lovebird.providershared.AccessMeansDTO;

@Value
public class AccessMeansRequest {

    private DefaultAuthenticationMeans authMeans;
    private AccessMeansDTO accessMeans;
    private DataProviderState providerState;
    private Signer signer;
}
