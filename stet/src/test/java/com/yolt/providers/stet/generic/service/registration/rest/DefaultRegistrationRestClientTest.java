package com.yolt.providers.stet.generic.service.registration.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.http.error.ExecutionSupplier;
import com.yolt.providers.stet.generic.mapper.registration.RegistrationRequestMapper;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import com.yolt.providers.stet.generic.service.registration.rest.error.DefaultRegistrationHttpErrorHandler;
import com.yolt.providers.stet.generic.service.registration.rest.header.RegistrationHttpHeadersFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.REGISTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(MockitoExtension.class)
class DefaultRegistrationRestClientTest {

    @Mock
    private RegistrationHttpHeadersFactory headersFactory;

    @Mock
    private RegistrationRequestMapper registrationRequestMapper;

    @Mock
    private DefaultProperties properties;

    @Mock
    private DefaultRegistrationHttpErrorHandler errorHandler;

    @Mock
    private HttpClient httpClient;

    @Captor
    private ArgumentCaptor<ExecutionSupplier<?>> executionSupplierArgumentCaptor;

    @Captor
    private ArgumentCaptor<ExecutionInfo> executionInfoArgumentCaptor;

    @InjectMocks
    private DefaultRegistrationRestClient restClient;

    @Test
    void shouldReturnProperResultForRegisterClientWithCorrectData() throws TokenInvalidException {
        // given
        String url = "https://example.com/registration";
        RegistrationRequest registrationRequest = mock(RegistrationRequest.class);
        String expectedBody = "body";
        HttpHeaders expectedHeaders = new HttpHeaders();
        ObjectNode expectedRegistrationResponse = mock(ObjectNode.class);
        ExecutionInfo expectedExecutionInfo = new ExecutionInfo(url, POST, expectedHeaders, REGISTER);

        when(properties.getRegistrationUrl())
                .thenReturn(url);
        when(registrationRequestMapper.mapToRegistrationRequest(any(RegistrationRequest.class)))
                .thenReturn(expectedBody);
        when(headersFactory.createRegistrationHttpHeaders(any(RegistrationRequest.class), any(Object.class), any(HttpMethod.class), anyString()))
                .thenReturn(expectedHeaders);
        when(errorHandler.executeAndHandle(ArgumentMatchers.<ExecutionSupplier<ObjectNode>>any(), any(ExecutionInfo.class)))
                .thenReturn(expectedRegistrationResponse);

        // when
        ObjectNode registrationResponse = restClient.registerClient(httpClient, registrationRequest);

        // then
        assertThat(registrationResponse).isEqualTo(expectedRegistrationResponse);
        verify(registrationRequestMapper).mapToRegistrationRequest(registrationRequest);
        verify(headersFactory).createRegistrationHttpHeaders(registrationRequest, expectedBody, POST, url);
        verify(errorHandler).executeAndHandle(executionSupplierArgumentCaptor.capture(), executionInfoArgumentCaptor.capture());
        assertThat(executionSupplierArgumentCaptor.getValue()).isNotNull();
        assertThat(executionInfoArgumentCaptor.getValue()).isEqualToComparingFieldByField(expectedExecutionInfo);
    }
}
