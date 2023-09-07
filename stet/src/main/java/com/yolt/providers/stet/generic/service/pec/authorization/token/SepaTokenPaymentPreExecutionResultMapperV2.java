package com.yolt.providers.stet.generic.service.pec.authorization.token;

import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.Region;

public interface SepaTokenPaymentPreExecutionResultMapperV2<PreExecutionResult> {

    PreExecutionResult map(InitiatePaymentRequest request,
                           DefaultAuthenticationMeans authMeans,
                           HttpClient httpClient,
                           Region region);

    PreExecutionResult map(GetStatusRequest request,
                           DefaultAuthenticationMeans authMeans,
                           HttpClient httpClient,
                           Region region);

    PreExecutionResult map(SubmitPaymentRequest request,
                           DefaultAuthenticationMeans authMeans,
                           HttpClient httpClient,
                           Region region);
}
