package com.yolt.providers.starlingbank.common.paymentexecutioncontext.model;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpClient;
import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Value
@Builder
public class StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult {

    Date expiresIn;
    String refreshToken;
    String token;
    String url;
    StarlingBankAuthenticationMeans authenticationMeans;
    Signer signer;
    StarlingBankHttpClient httpClient;
    String externalPaymentId;
    InitiateUkDomesticPaymentRequestDTO paymentRequest;
    String redirectUrlPostedBackFromSite;
}
