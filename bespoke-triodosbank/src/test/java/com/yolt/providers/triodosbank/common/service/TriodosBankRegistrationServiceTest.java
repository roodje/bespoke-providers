package com.yolt.providers.triodosbank.common.service;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.triodosbank.common.model.RegistrationLinks;
import com.yolt.providers.triodosbank.common.model.http.RegistrationRequest;
import com.yolt.providers.triodosbank.common.model.http.RegistrationResponse;
import com.yolt.providers.triodosbank.common.model.http.RegistrationTokenResponse;
import com.yolt.providers.triodosbank.common.rest.TriodosBankHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_SECRET_STRING;
import static com.yolt.providers.triodosbank.common.auth.TriodosBankAuthenticationMeans.CLIENT_ID_STRING_NAME;
import static com.yolt.providers.triodosbank.common.auth.TriodosBankAuthenticationMeans.CLIENT_SECRET_STRING_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TriodosBankRegistrationServiceTest {

    private static final List<String> BASE_CLIENT_REDIRECT_URL = List.of("https://yolt.com/callback", "https://yolt.com/dev");
    private static final String PROVIDER = "provider";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String REGISTRATION_URL = "registrationUrl.com";

    @Mock
    private TriodosBankHttpClient httpClient;

    private TriodosBankRegistrationService bankRegistrationService;

    @BeforeEach
    public void setup() {
        bankRegistrationService = new TriodosBankRegistrationService();
    }

    @Test
    public void shouldRegisterCustomer() {
        // given
        RegistrationRequest registrationRequest = RegistrationRequest.builder().
                registrationToken(ACCESS_TOKEN).
                sectorIdentifierUri("").
                redirectUris(BASE_CLIENT_REDIRECT_URL).
                build();

        RegistrationResponse registrationResponse = new RegistrationResponse();
        registrationResponse.setClientId(CLIENT_ID);
        registrationResponse.setClientSecret(CLIENT_SECRET);

        RegistrationTokenResponse registrationTokenResponse = createRegistrationTokenResponse();

        when(httpClient.getRegistrationToken()).thenReturn(registrationTokenResponse);
        when(httpClient.getRegistrationResponse(REGISTRATION_URL, registrationRequest)).thenReturn(registrationResponse);

        // when
        Map<String, BasicAuthenticationMean> registration = bankRegistrationService.register(
                httpClient,
                BASE_CLIENT_REDIRECT_URL,
                PROVIDER
        );

        // then
        BasicAuthenticationMean clientIdAuthMean = registration.get(CLIENT_ID_STRING_NAME);
        assertThat(clientIdAuthMean.getValue()).isEqualTo(CLIENT_ID);
        assertThat(clientIdAuthMean.getType()).isEqualTo(CLIENT_ID_STRING.getType());

        BasicAuthenticationMean clientSecretAuthMean = registration.get(CLIENT_SECRET_STRING_NAME);
        assertThat(clientSecretAuthMean.getValue()).isEqualTo(CLIENT_SECRET);
        assertThat(clientSecretAuthMean.getType()).isEqualTo(CLIENT_SECRET_STRING.getType());
    }

    private RegistrationTokenResponse createRegistrationTokenResponse() {
        RegistrationTokenResponse registrationToken = new RegistrationTokenResponse();
        registrationToken.setAccessToken(ACCESS_TOKEN);
        RegistrationLinks links = new RegistrationLinks();
        links.setRegistration(REGISTRATION_URL);
        registrationToken.setLinks(links);
        return registrationToken;
    }

}
