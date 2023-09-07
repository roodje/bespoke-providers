package com.yolt.providers.stet.generic.service.pec.authorization.token;

import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
@Deprecated
public interface SepaTokenPaymentPreExecutionResultMapper<PreExecutionResult> {

    PreExecutionResult map(InitiatePaymentRequest request,
                           DefaultAuthenticationMeans authMeans);

    PreExecutionResult map(GetStatusRequest request,
                           DefaultAuthenticationMeans authMeans);

    PreExecutionResult map(SubmitPaymentRequest request,
                           DefaultAuthenticationMeans authMeans);
}
