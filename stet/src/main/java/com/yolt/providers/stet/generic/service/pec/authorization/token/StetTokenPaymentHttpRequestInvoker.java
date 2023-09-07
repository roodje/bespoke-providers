package com.yolt.providers.stet.generic.service.pec.authorization.token;

import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentHttpHeadersFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT;

@RequiredArgsConstructor
@Deprecated
public class StetTokenPaymentHttpRequestInvoker implements SepaTokenPaymentHttpRequestInvoker<StetTokenPaymentPreExecutionResult> {

    protected final SepaTokenPaymentHttpRequestBodyProvider httpRequestBodyProvider;
    protected final StetPaymentHttpHeadersFactory httpHeadersFactory;
    protected final HttpClientFactory httpClientFactory;
    protected final ProviderIdentification providerIdentification;

    @SneakyThrows
    @Override
    public TokenResponseDTO invokeRequest(StetTokenPaymentPreExecutionResult preExecutionResult) {
        HttpClient httpClient = httpClientFactory.createHttpClient(
                preExecutionResult.getRestTemplateManager(),
                preExecutionResult.getAuthMeans(),
                preExecutionResult.getBaseUrl(),
                providerIdentification.getDisplayName());

        MultiValueMap<String, String> requestBody = httpRequestBodyProvider.createRequestBody(preExecutionResult.getAuthMeans());
        HttpHeaders headers = httpHeadersFactory.createPaymentAccessTokenHttpHeaders(preExecutionResult, requestBody);

        return httpClient.exchangeForBody(
                preExecutionResult.getRequestUrl(),
                HttpMethod.POST,
                httpRequestBodyProvider.createHttpEntity(requestBody, headers),
                CLIENT_CREDENTIALS_GRANT,
                TokenResponseDTO.class);
    }
}
