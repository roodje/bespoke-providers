package com.yolt.providers.dkbgroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.dkbgroup.common.model.api.*;
import com.yolt.providers.dkbgroup.common.model.authorization.ValidateCredentialsAuthRequest;
import com.yolt.providers.dkbgroup.common.model.authorization.ValidationResponse;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class DKBGroupHttpClient extends DefaultHttpClient {

    private static final String TOKEN_ENDPOINT = "/psd2-auth/v1/auth/token";
    private static final String CONSENTS_ENDPOINT = "/v1/consents";
    private static final String AUTHORISATIONS_ENDPOINT_TEMPLATE = "/v1/consents/%s/authorisations";
    private static final String UPDATE_AUTHORISATION_ENDPOINT_TEMPLATE = "/v1/consents/%s/authorisations/%s";

    public DKBGroupHttpClient(final MeterRegistry registry,
                              final RestTemplate restTemplate,
                              final String provider) {
        super(registry, restTemplate, provider);
    }

    public ValidationResponse postForToken(final HttpHeaders headers,
                                           final ValidateCredentialsAuthRequest body) throws TokenInvalidException {
        return exchangeForBody(TOKEN_ENDPOINT, HttpMethod.POST, new HttpEntity<>(body, headers),
                ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT, ValidationResponse.class);
    }

    public ConsentsResponse201 postForConsentId(final HttpHeaders headers,
                                                final Consents body) throws TokenInvalidException {
        return exchangeForBody(CONSENTS_ENDPOINT, HttpMethod.POST, new HttpEntity<>(body, headers),
                ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT, ConsentsResponse201.class);
    }

    public StartScaprocessResponse postForAuthorisationId(final HttpHeaders headers,
                                                          final String consentId) throws TokenInvalidException {
        return exchangeForBody(String.format(AUTHORISATIONS_ENDPOINT_TEMPLATE, consentId), HttpMethod.POST, new HttpEntity<>(headers),
                ProviderClientEndpoints.GET_ACCOUNT_REQUEST_ID, StartScaprocessResponse.class);
    }

    public SelectPsuAuthenticationMethodResponse chooseSCAMethod(final HttpHeaders headers,
                                                                 final SelectPsuAuthenticationMethod body,
                                                                 final String consentId,
                                                                 final String authorisationId) throws TokenInvalidException {
        return exchangeForBody(String.format(UPDATE_AUTHORISATION_ENDPOINT_TEMPLATE, consentId, authorisationId), HttpMethod.PUT,
                new HttpEntity<>(body, headers), ProviderClientEndpoints.RETRIEVE_ACCOUNT_REQUEST_ID, SelectPsuAuthenticationMethodResponse.class);
    }

    public AuthorisationConfirmationResponse authoriseConsent(final HttpHeaders headers,
                                                              final AuthorisationConfirmation body,
                                                              final String consentId,
                                                              final String authorisationId) throws TokenInvalidException {
        return exchangeForBody(String.format(UPDATE_AUTHORISATION_ENDPOINT_TEMPLATE, consentId, authorisationId), HttpMethod.PUT,
                new HttpEntity<>(body, headers), ProviderClientEndpoints.GET_ACCESS_TOKEN, AuthorisationConfirmationResponse.class);
    }
}
