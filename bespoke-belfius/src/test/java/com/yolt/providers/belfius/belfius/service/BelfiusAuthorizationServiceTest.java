package com.yolt.providers.belfius.belfius.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.belfius.common.exception.UnexpectedJsonElementException;
import com.yolt.providers.belfius.common.http.client.BelfiusGroupHttpClient;
import com.yolt.providers.belfius.common.http.client.BelfiusGroupTokenHttpClient;
import com.yolt.providers.belfius.common.model.BelfiusGroupProviderState;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.exception.MissingDataException;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BelfiusAuthorizationServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private BelfiusGroupHttpClient belfiusGroupHttpClient;

    @Mock
    private BelfiusGroupTokenHttpClient belfiusGroupTokenHttpClient;

    private BelfiusAuthorizationService service;

    @BeforeEach
    public void setup() {
        service = new BelfiusAuthorizationService(objectMapper);
    }

    @Test
    public void shouldThrowUnexpectedJsonElementExceptionWhenUnableToSerializeBelfiusGroupProviderState() throws JsonProcessingException {
        // given
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("ConsentLanguage", "en");
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .build();

        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        // when
        Throwable thrown = catchThrowable(() -> service.getLoginUrlForUser(belfiusGroupHttpClient, urlCreateAccessMeans));

        // then
        assertThat(thrown).isInstanceOf(UnexpectedJsonElementException.class);
    }

    @Test
    public void shouldCallGetAccessTokenEndpointWithRedirectUrlCodeReturnedFromBankAndCodeVerifier() throws JsonProcessingException {
        // given
        String redirectUrl = "http://www.yolt.com/callback";
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setRedirectUrlPostedBackFromSite("http://www.yolt.com/callback?code=SOME_CODE&state=SOME_STATE")
                .setBaseClientRedirectUrl(redirectUrl)
                .setProviderState("CODE_VERIFIER")
                .build();
        when(objectMapper.readValue(anyString(), eq(BelfiusGroupProviderState.class))).thenReturn(new BelfiusGroupProviderState("en", "CODE_VERIFIER"));

        // when
        service.getAccessToken(belfiusGroupTokenHttpClient, urlCreateAccessMeans);

        // then
        verify(belfiusGroupTokenHttpClient).getAccessToken(redirectUrl, "SOME_CODE", "CODE_VERIFIER");
    }

    @Test
    public void shouldThrowMissingDataExceptionWhenCodeParameterWillBeMissing() {
        // given
        String redirectUrl = "http://www.yolt.com/callback";
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setRedirectUrlPostedBackFromSite("http://www.yolt.com/callback?error=SOME_ERROR_MESSAGE")
                .setBaseClientRedirectUrl(redirectUrl)
                .setProviderState("CODE_VERIFIER")
                .build();

        // when
        Throwable thrown = catchThrowable(() -> service.getAccessToken(belfiusGroupTokenHttpClient, urlCreateAccessMeans));

        // then
        assertThat(thrown).isInstanceOf(MissingDataException.class);
    }
}