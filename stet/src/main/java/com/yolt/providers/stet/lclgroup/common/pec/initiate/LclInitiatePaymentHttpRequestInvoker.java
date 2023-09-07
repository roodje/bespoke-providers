package com.yolt.providers.stet.lclgroup.common.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationRequestDTO;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePaymentHttpRequestInvokerV2;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.INITIATE_PAYMENT;

public class LclInitiatePaymentHttpRequestInvoker extends StetInitiatePaymentHttpRequestInvokerV2 {

    private final HttpErrorHandlerV2 errorHandler;
    private final LclGroupPaymentHeadersExtractor headersExtractor;

    public LclInitiatePaymentHttpRequestInvoker(HttpErrorHandlerV2 errorHandler,
                                                LclGroupPaymentHeadersExtractor headersExtractor) {
        super(errorHandler);
        this.errorHandler = errorHandler;
        this.headersExtractor = headersExtractor;
    }


    @SneakyThrows
    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<StetPaymentInitiationRequestDTO> httpEntity,
                                                  StetInitiatePreExecutionResult preExecutionResult) {

        HttpClient httpClient = preExecutionResult.getHttpClient();

        ResponseEntity<JsonNode> response =  httpClient.exchange(
                preExecutionResult.getRequestPath(),
                preExecutionResult.getHttpMethod(),
                httpEntity,
                INITIATE_PAYMENT,
                JsonNode.class,
                errorHandler);

        headersExtractor.setHeaders(response);
        return response;
    }
}
