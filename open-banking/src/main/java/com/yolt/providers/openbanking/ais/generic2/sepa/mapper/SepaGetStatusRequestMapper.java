package com.yolt.providers.openbanking.ais.generic2.sepa.mapper;

import com.yolt.providers.common.pis.common.GetStatusRequest;

public class SepaGetStatusRequestMapper {

    public GetStatusRequest map(com.yolt.providers.common.pis.sepa.GetStatusRequest sepaRequest) {
        return new GetStatusRequest(
                sepaRequest.getProviderState(),
                sepaRequest.getPaymentId(),
                sepaRequest.getAuthenticationMeans(),
                sepaRequest.getSigner(),
                sepaRequest.getRestTemplateManager(),
                sepaRequest.getPsuIpAddress(),
                sepaRequest.getAuthenticationMeansReference()
        );
    }
}
