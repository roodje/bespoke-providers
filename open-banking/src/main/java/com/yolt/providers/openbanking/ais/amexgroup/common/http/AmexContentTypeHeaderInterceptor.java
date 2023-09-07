package com.yolt.providers.openbanking.ais.amexgroup.common.http;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class AmexContentTypeHeaderInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        MediaType contentType = request.getHeaders().getContentType();
        if (ObjectUtils.isNotEmpty(contentType)) {
            String type = contentType.getType();
            String subtype = contentType.getSubtype();
            request.getHeaders().setContentType(new MediaType(type, subtype));
        }
        return execution.execute(request, body);
    }
}
