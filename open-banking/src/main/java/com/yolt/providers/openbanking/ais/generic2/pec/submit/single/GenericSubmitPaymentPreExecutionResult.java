package com.yolt.providers.openbanking.ais.generic2.pec.submit.single;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import lombok.Data;
import nl.ing.lovebird.providershared.ProviderPayment;

@Data
public class GenericSubmitPaymentPreExecutionResult {

    private UkProviderState providerState;
    private String accessToken;
    private DefaultAuthMeans authMeans;
    private Signer signer;
    private RestTemplateManager restTemplateManager;
    private String externalPaymentId;
    private ProviderPayment providerPayment;
}
