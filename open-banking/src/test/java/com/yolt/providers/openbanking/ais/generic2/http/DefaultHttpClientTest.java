package com.yolt.providers.openbanking.ais.generic2.http;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultHttpClientTest {

    private static final String SOME_PROVIDER = "SOME_PROVIDER";
    private static final String SOME_BODY = "SOME_BODY";
    private static final String SOME_ENDPOINT = "/endpoint";
    private static final String SOME_ERROR_MESSAGE = "SOME_ERROR";

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private HttpErrorHandler httpErrorHandler;

    private DefaultHttpClient defaultHttpClient;
    private MeterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        defaultHttpClient = new DefaultHttpClient(registry, restTemplate, SOME_PROVIDER);
    }

    @Test
    void shouldReturnResponseEntityForSuccessfulResponse() throws TokenInvalidException {
        //given
        HttpEntity<String> httpEntity = new HttpEntity<>(SOME_BODY);
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.exchange(SOME_ENDPOINT, HttpMethod.GET, httpEntity, String.class))
                .thenReturn(expectedResponse);

        //when
        ResponseEntity<String> result = defaultHttpClient.exchange(SOME_ENDPOINT, HttpMethod.GET, httpEntity,
                ProviderClientEndpoints.GET_ACCOUNTS, String.class, httpErrorHandler);

        //then
        assertThat(result).isEqualTo(expectedResponse);
        verify(httpErrorHandler, never()).handle(any(HttpStatusCodeException.class));
        assertThat(registry.getMeters().get(0).getId().getName()).isEqualTo("restclient_openbanking_request_duration");
        assertThat(registry.getMeters().get(0).getId().getTag("http_status")).isEqualTo("200 OK");
        assertThat(registry.getMeters().get(0).getId().getTag("service_name")).isEqualTo("SOME_PROVIDER");
        assertThat(registry.getMeters().get(0).getId().getTag("service_path")).isEqualTo("get_accounts");
    }

    @Test
    void shouldReturnNullResponseWhenHttpStatusCodeExceptionWillOccur() throws TokenInvalidException {
        //given
        HttpEntity<String> httpEntity = new HttpEntity<>(SOME_BODY);
        HttpClientErrorException expectedError = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(SOME_ENDPOINT, HttpMethod.GET, httpEntity, String.class))
                .thenThrow(expectedError);

        //when
        ResponseEntity<String> result = defaultHttpClient.exchange(SOME_ENDPOINT, HttpMethod.GET, httpEntity,
                ProviderClientEndpoints.GET_ACCOUNTS, String.class, httpErrorHandler);

        //then
        assertThat(result).isNull();
        verify(httpErrorHandler).handle(expectedError);
        assertThat(registry.getMeters().get(0).getId().getName()).isEqualTo("restclient_openbanking_request_duration");
        assertThat(registry.getMeters().get(0).getId().getTag("http_status")).isEqualTo("400 BAD_REQUEST");
        assertThat(registry.getMeters().get(0).getId().getTag("service_name")).isEqualTo("SOME_PROVIDER");
        assertThat(registry.getMeters().get(0).getId().getTag("service_path")).isEqualTo("get_accounts");
    }

    @Test
    void shouldRegisterJsonParseErrorResponseStatusForMismatchedInputException() {
        //given
        HttpEntity<String> httpEntity = new HttpEntity<>(SOME_BODY);
        RestClientException restClientException = new RestClientException(SOME_ERROR_MESSAGE, MismatchedInputException.from(mock(JsonParser.class), String.class, SOME_ERROR_MESSAGE));
        when(restTemplate.exchange(SOME_ENDPOINT, HttpMethod.GET, httpEntity, String.class))
                .thenThrow(restClientException);

        //when
        ThrowableAssert.ThrowingCallable result = () -> defaultHttpClient.exchange(SOME_ENDPOINT, HttpMethod.GET, httpEntity,
                ProviderClientEndpoints.GET_ACCOUNTS, String.class, httpErrorHandler);

        //then
        assertThatThrownBy(result)
                .isInstanceOf(RestClientException.class)
                .getCause()
                .isInstanceOf(MismatchedInputException.class);
        assertThat(registry.getMeters().get(0).getId().getName()).isEqualTo("restclient_openbanking_request_duration");
        assertThat(registry.getMeters().get(0).getId().getTag("http_status")).isEqualTo("JSON_PARSE_ERROR");
        assertThat(registry.getMeters().get(0).getId().getTag("service_name")).isEqualTo("SOME_PROVIDER");
        assertThat(registry.getMeters().get(0).getId().getTag("service_path")).isEqualTo("get_accounts");
    }

    @Test
    void shouldRegisterMinusOneResponseStatusForOtherRestClientExceptions() {
        //given
        HttpEntity<String> httpEntity = new HttpEntity<>(SOME_BODY);
        RestClientException restClientException = new RestClientException(SOME_ERROR_MESSAGE);
        when(restTemplate.exchange(SOME_ENDPOINT, HttpMethod.GET, httpEntity, String.class))
                .thenThrow(restClientException);

        //when
        ThrowableAssert.ThrowingCallable result = () -> defaultHttpClient.exchange(SOME_ENDPOINT, HttpMethod.GET, httpEntity,
                ProviderClientEndpoints.GET_ACCOUNTS, String.class, httpErrorHandler);

        //then
        assertThatThrownBy(result)
                .isInstanceOf(RestClientException.class);
        assertThat(registry.getMeters().get(0).getId().getName()).isEqualTo("restclient_openbanking_request_duration");
        assertThat(registry.getMeters().get(0).getId().getTag("http_status")).isEqualTo("-1");
        assertThat(registry.getMeters().get(0).getId().getTag("service_name")).isEqualTo("SOME_PROVIDER");
        assertThat(registry.getMeters().get(0).getId().getTag("service_path")).isEqualTo("get_accounts");
    }
}