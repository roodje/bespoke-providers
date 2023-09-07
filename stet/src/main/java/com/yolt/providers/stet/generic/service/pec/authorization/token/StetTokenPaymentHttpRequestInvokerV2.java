package com.yolt.providers.stet.generic.service.pec.authorization.token;

import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentHttpHeadersFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT;

@RequiredArgsConstructor
public class StetTokenPaymentHttpRequestInvokerV2 implements SepaTokenPaymentHttpRequestInvoker<StetTokenPaymentPreExecutionResult> {

    protected final SepaTokenPaymentHttpRequestBodyProvider httpRequestBodyProvider;
    protected final StetPaymentHttpHeadersFactory httpHeadersFactory;
    protected final HttpErrorHandlerV2 errorHandlerV2;

    @SneakyThrows
    @Override
    public TokenResponseDTO invokeRequest(StetTokenPaymentPreExecutionResult preExecutionResult) {
        HttpClient httpClient = preExecutionResult.getHttpClient();

        MultiValueMap<String, String> requestBody = httpRequestBodyProvider.createRequestBody(preExecutionResult.getAuthMeans());
        HttpHeaders headers = httpHeadersFactory.createPaymentAccessTokenHttpHeaders(preExecutionResult, requestBody);

        return httpClient.exchange(
                preExecutionResult.getRequestUrl(),
                HttpMethod.POST,
                httpRequestBodyProvider.createHttpEntity(requestBody, headers),
                CLIENT_CREDENTIALS_GRANT,
                TokenResponseDTO.class,
                errorHandlerV2).getBody();
    }
}
