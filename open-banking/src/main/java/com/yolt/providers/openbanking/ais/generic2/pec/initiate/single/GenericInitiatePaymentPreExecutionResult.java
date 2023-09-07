package com.yolt.providers.openbanking.ais.generic2.pec.initiate.single;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import lombok.Data;
import nl.ing.lovebird.providershared.ProviderPayment;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;

@Data
public class GenericInitiatePaymentPreExecutionResult {

    private DefaultAuthMeans authMeans;
    private String externalPaymentId;
    private String state;
    private String baseClientRedirectUrl;
    private Signer signer;
    private InitiateUkDomesticPaymentRequestDTO paymentRequestDTO;
    private ProviderPayment payment;
    private RestTemplateManager restTemplateManager;
    private String accessToken;
    private AuthenticationMeansReference authenticationMeansReference;
}
