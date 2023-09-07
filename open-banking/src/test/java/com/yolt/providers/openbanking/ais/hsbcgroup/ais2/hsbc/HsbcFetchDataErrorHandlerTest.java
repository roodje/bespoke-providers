package com.yolt.providers.openbanking.ais.hsbcgroup.ais2.hsbc;

import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupApp;
import com.yolt.providers.openbanking.ais.hsbcgroup.hsbc.common.http.HsbcRestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadAccount6;
import io.micrometer.core.instrument.MeterRegistry;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {HsbcGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("hsbc-generic")
@ExtendWith(MockitoExtension.class)
public class HsbcFetchDataErrorHandlerTest {

    private static final String NULL_MESSAGE_FIELD = "{\"message\":null}";
    private static final String NON_NULL_MESSAGE_FIELD = "{\"message\":value}";
    private static final String serviceName = "TestName";
    private static final String path = "Path";
    private static final String institutionId = "1234";
    private static final Class<OBReadAccount6> type = OBReadAccount6.class;
    private static HsbcRestClient hsbcRestClient;
    private static HttpEntity entity;
    private static AccessMeans accessMeans;
    private static PaymentRequestSigner payloadSigner;
    private static HttpClient httpClient;

    @Mock
    RestTemplate restTemplate;

    @Autowired
    MeterRegistry meterRegistry;

    @BeforeEach
    public void setup() {
        hsbcRestClient = new HsbcRestClient(payloadSigner);
        accessMeans = new AccessMeans();
        entity = new HttpEntity<>(getHeaders(accessMeans, institutionId));
    }

    @Test
    public void shouldCallTwoTimes() {

        //given
        HttpStatusCodeException mockedException = Mockito.mock(HttpStatusCodeException.class);
        Mockito.when(mockedException.getResponseBodyAsString()).thenReturn(NULL_MESSAGE_FIELD);
        Mockito.when(mockedException.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        Mockito.when(restTemplate.exchange(path, HttpMethod.GET, entity, type))
                .thenThrow(mockedException);
        httpClient = new DefaultHttpClient(meterRegistry, restTemplate, serviceName);
        //when
        Assertions.assertThatThrownBy(() -> hsbcRestClient.fetchAccounts(httpClient, path, accessMeans, institutionId, type))
                .isInstanceOf(HttpStatusCodeException.class);
        //then
        Mockito.verify(restTemplate, Mockito.times(2))
                .exchange(path, HttpMethod.GET, entity, type);
    }

    @Test
    public void shouldCallOnlyOneTime() {
        //given
        HttpStatusCodeException mockedException = Mockito.mock(HttpStatusCodeException.class);
        Mockito.when(mockedException.getResponseBodyAsString()).thenReturn(NON_NULL_MESSAGE_FIELD);
        Mockito.when(mockedException.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        Mockito.when(restTemplate.exchange(path, HttpMethod.GET, entity, type))
                .thenThrow(mockedException);
        httpClient = new DefaultHttpClient(meterRegistry, restTemplate, serviceName);
        //when
        Assertions.assertThatThrownBy(() -> hsbcRestClient.fetchAccounts(httpClient, path, accessMeans, institutionId, type))
                .isInstanceOf(HttpStatusCodeException.class);
        //then
        Mockito.verify(restTemplate, Mockito.times(1))
                .exchange(path, HttpMethod.GET, entity, type);
    }

    private HttpHeaders getHeaders(final AccessMeans clientAccessToken,
                                   final String institutionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(clientAccessToken.getAccessToken());
        headers.add(HttpExtraHeaders.FINANCIAL_ID_HEADER_NAME, institutionId);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }
}

