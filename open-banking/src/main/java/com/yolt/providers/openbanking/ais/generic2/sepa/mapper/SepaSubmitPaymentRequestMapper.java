package com.yolt.providers.openbanking.ais.generic2.sepa.mapper;

import com.yolt.providers.common.pis.common.SubmitPaymentRequest;

public class SepaSubmitPaymentRequestMapper {

    public SubmitPaymentRequest map(com.yolt.providers.common.pis.sepa.SubmitPaymentRequest sepaRequest) {
        return new SubmitPaymentRequest(
                sepaRequest.getProviderState(),
                sepaRequest.getAuthenticationMeans(),
                sepaRequest.getRedirectUrlPostedBackFromSite(),
                sepaRequest.getSigner(),
                sepaRequest.getRestTemplateManager(),
                sepaRequest.getPsuIpAddress(),
                sepaRequest.getAuthenticationMeansReference()
        );
    }
}
