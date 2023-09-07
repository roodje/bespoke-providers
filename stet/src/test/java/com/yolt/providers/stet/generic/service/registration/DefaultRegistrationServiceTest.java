package com.yolt.providers.stet.generic.service.registration;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import com.yolt.providers.stet.generic.service.registration.rest.RegistrationRestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class DefaultRegistrationServiceTest {

    @InjectMocks
    private DefaultRegistrationService sut;

    @Mock
    private RegistrationRestClient registrationRestClient;

    @Mock
    private ExtendedAuthenticationMeansSupplier extendedAuthenticationMeansSupplier;

    @Mock
    private HttpClient httpClient;

    @Test
    void shouldReturnEmptyMapForRegisterWhenClientIdIsAlreadyInAuthMeans() {
        // given
        RegistrationRequest registrationRequest = mock(RegistrationRequest.class);
        DefaultAuthenticationMeans authMeans = mock(DefaultAuthenticationMeans.class);
        when(registrationRequest.getAuthMeans())
                .thenReturn(authMeans);
        when(authMeans.getClientId())
                .thenReturn("clientId");

        // when
        Map<String, BasicAuthenticationMean> result = sut.register(httpClient, registrationRequest);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnRegistrationAuthMeansForRegisterWhenClientIdIsNotInAuthMeans() {
        // given
        RegistrationRequest registrationRequest = mock(RegistrationRequest.class);
        DefaultAuthenticationMeans authMeans = mock(DefaultAuthenticationMeans.class);
        Map<String, TypedAuthenticationMeans> autoconfiguredMeans = new HashMap<>();
        autoconfiguredMeans.put("client-id", TypedAuthenticationMeans.CLIENT_ID_STRING);
        autoconfiguredMeans.put("client-secret", TypedAuthenticationMeans.CLIENT_SECRET_STRING);

        Map<String, String> registeredAuthMeans = new HashMap<>();
        registeredAuthMeans.put("client-id", "fakeClientId");
        registeredAuthMeans.put("client-secret", "fakeClientSecret");

        ObjectNode expectedAuthMeansMap = new ObjectNode(JsonNodeFactory.instance);

        when(registrationRequest.getAuthMeans())
                .thenReturn(authMeans);
        when(authMeans.getClientId())
                .thenReturn(null);
        when(registrationRestClient.registerClient(any(HttpClient.class), any(RegistrationRequest.class)))
                .thenReturn(expectedAuthMeansMap);
        when(extendedAuthenticationMeansSupplier.getAutoConfiguredTypedAuthMeans())
                .thenReturn(autoconfiguredMeans);
        when(extendedAuthenticationMeansSupplier.getRegisteredAuthMeans(any(ObjectNode.class)))
                .thenReturn(registeredAuthMeans);

        // when
        Map<String, BasicAuthenticationMean> result = sut.register(httpClient, registrationRequest);

        // then
        assertThat(result)
                .hasEntrySatisfying("client-id", basicAuthenticationMean -> assertThat(basicAuthenticationMean.getValue()).isEqualTo("fakeClientId"))
                .hasEntrySatisfying("client-secret", basicAuthenticationMean -> assertThat(basicAuthenticationMean.getValue()).isEqualTo("fakeClientSecret"));
        verify(registrationRestClient).registerClient(httpClient, registrationRequest);
        verify(extendedAuthenticationMeansSupplier).getAutoConfiguredTypedAuthMeans();
    }
}
