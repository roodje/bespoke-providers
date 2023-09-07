package com.yolt.providers.stet.lclgroup.common.pec.authorization;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import org.springframework.web.util.UriComponentsBuilder;

public class LclGroupPaymentAuthorizationUrlExtractor implements PaymentAuthorizationUrlExtractor<StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> {

    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    private static final String STATE_PARAM = "state";

    @Override
    public String extractAuthorizationUrl(StetPaymentInitiationResponseDTO responseDTO,
                                          StetInitiatePreExecutionResult preExecutionResult) {

        String callBackUrlWithState = UriComponentsBuilder
                .fromUriString(preExecutionResult.getBaseClientRedirectUrl())
                .queryParam(STATE_PARAM, preExecutionResult.getState())
                .toUriString();

        return UriComponentsBuilder
                .fromUriString(responseDTO.getLinks().getConsentApproval().getHref())
                .queryParam(CLIENT_ID_PARAM, preExecutionResult.getAuthMeans().getClientId())
                .queryParam(REDIRECT_URI_PARAM, callBackUrlWithState)
                .build()
                .toUriString();
    }
}
