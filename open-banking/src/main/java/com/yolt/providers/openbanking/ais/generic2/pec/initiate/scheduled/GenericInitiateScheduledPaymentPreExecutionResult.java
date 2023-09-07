package com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequestDTO;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import lombok.Data;
import nl.ing.lovebird.providershared.ProviderPayment;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;

@Data
public class GenericInitiateScheduledPaymentPreExecutionResult {

    private DefaultAuthMeans authMeans;
    private String externalPaymentId;
    private String state;
    private String baseClientRedirectUrl;
    private Signer signer;
    private InitiateUkDomesticScheduledPaymentRequestDTO paymentRequestDTO;
    private ProviderPayment payment;
    private RestTemplateManager restTemplateManager;
    private String accessToken;
    private AuthenticationMeansReference authenticationMeansReference;

}
