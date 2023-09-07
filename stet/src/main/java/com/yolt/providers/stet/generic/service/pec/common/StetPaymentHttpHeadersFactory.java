package com.yolt.providers.stet.generic.service.pec.common;

import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationRequestDTO;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

public interface StetPaymentHttpHeadersFactory {

    HttpHeaders createPaymentAccessTokenHttpHeaders(StetTokenPaymentPreExecutionResult preExecutionResult, MultiValueMap<String, String> requestBody);

    HttpHeaders createPaymentInitiationHttpHeaders(StetInitiatePreExecutionResult preExecutionResult, StetPaymentInitiationRequestDTO requestDTO);

    HttpHeaders createPaymentSubmitHttpHeaders(StetConfirmationPreExecutionResult preExecutionResult, StetPaymentConfirmationRequestDTO requestDTO);

    HttpHeaders createPaymentStatusHttpHeaders(StetConfirmationPreExecutionResult preExecutionResult);
}
