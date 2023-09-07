package com.yolt.providers.openbanking.ais.generic2.pec.submit.single;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;

public class GenericUkSubmitPaymentPreExecutionResultFixture {

    public static GenericSubmitPaymentPreExecutionResult forUkDomesticSubmit(DefaultAuthMeans authMeans, UkProviderState providerState, String accessToken, RestTemplateManager restTemplateManager, Signer signer) {
        GenericSubmitPaymentPreExecutionResult fixture = new GenericSubmitPaymentPreExecutionResult();
        fixture.setAuthMeans(authMeans);
        fixture.setAccessToken(accessToken);
        fixture.setProviderState(providerState);
        fixture.setRestTemplateManager(restTemplateManager);
        fixture.setSigner(signer);
        return fixture;
    }
}
