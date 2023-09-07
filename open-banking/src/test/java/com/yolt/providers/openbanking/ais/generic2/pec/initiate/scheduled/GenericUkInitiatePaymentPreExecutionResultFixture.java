package com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.pec.initiate.single.GenericInitiatePaymentPreExecutionResult;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;

public class GenericUkInitiatePaymentPreExecutionResultFixture {

    public static GenericInitiatePaymentPreExecutionResult forUkDomesticInitiation(DefaultAuthMeans authMeans,
                                                                                   String state,
                                                                                   String baseClientRedirectUrl,
                                                                                   InitiateUkDomesticPaymentRequestDTO requestDTO,
                                                                                   AuthenticationMeansReference authenticationMeansReference,
                                                                                   String accessToken,
                                                                                   RestTemplateManager restTemplateManager,
                                                                                   Signer signer) {
        GenericInitiatePaymentPreExecutionResult fixture = new GenericInitiatePaymentPreExecutionResult();
        fixture.setAuthMeans(authMeans);
        fixture.setAuthenticationMeansReference(authenticationMeansReference);
        fixture.setState(state);
        fixture.setBaseClientRedirectUrl(baseClientRedirectUrl);
        fixture.setPaymentRequestDTO(requestDTO);
        fixture.setAccessToken(accessToken);
        fixture.setSigner(signer);
        fixture.setRestTemplateManager(restTemplateManager);
        return fixture;
    }
}
