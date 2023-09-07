package com.yolt.providers.stet.generic.service.fetchData.rest.header;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataNoSigningHttpHeadersFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Map;
import java.util.function.Supplier;

import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.PSU_IP_ADDRESS;
import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.X_REQUEST_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.cloud.contract.spec.internal.MediaTypes.APPLICATION_JSON;
import static org.springframework.http.HttpHeaders.*;

@ExtendWith(MockitoExtension.class)
class FetchDataNoSigningHttpHeadersFactoryTest {

    private static final String ACCESS_TOKEN = "b6b4ffe8-5e60-4ef9-858f-78b48c5d7b11";
    private static final String LAST_EXTERNAL_TRACE_ID = "8bf52479-f18c-40cd-a1f3-3e667015f05a";
    private static final String PSU_IP_ADDRESS_VALUE = "127.0.0.1";

    @Mock
    private Signer signer;

    private FetchDataNoSigningHttpHeadersFactory httpHeadersFactory;

    @BeforeEach
    void initialize() {
        Supplier<String> lastExternalTraceIdSupplier = () -> LAST_EXTERNAL_TRACE_ID;
        httpHeadersFactory = new FetchDataNoSigningHttpHeadersFactory(lastExternalTraceIdSupplier);
    }

    @Test
    void shouldReturnSpecifiedNoSigningHeadersForFetchingData() {
        // given
        DataRequest dataRequest = createDataRequest();

        // when
        HttpHeaders headers = httpHeadersFactory.createFetchDataHeaders("/accounts", dataRequest, HttpMethod.GET);

        // then
        Map<String, String> headersMap = headers.toSingleValueMap();
        assertThat(headersMap).hasSize(5);
        assertThat(headersMap).containsKey(ACCEPT).containsValue(APPLICATION_JSON);
        assertThat(headersMap).containsKey(AUTHORIZATION).containsValue("Bearer " + ACCESS_TOKEN);
        assertThat(headersMap).containsKey(CONTENT_TYPE).containsValue(APPLICATION_JSON);
        assertThat(headersMap).containsKey(PSU_IP_ADDRESS).containsValue(PSU_IP_ADDRESS_VALUE);
        assertThat(headersMap).containsKey(X_REQUEST_ID).containsValue(LAST_EXTERNAL_TRACE_ID);
    }

    private DataRequest createDataRequest() {
        DefaultAuthenticationMeans authMeans = createAuthenticationMeans();
        return new DataRequest("http://localhost", signer, authMeans, ACCESS_TOKEN, PSU_IP_ADDRESS_VALUE, false);
    }

    private DefaultAuthenticationMeans createAuthenticationMeans() {
        return DefaultAuthenticationMeans.builder()
                .build();
    }
}
