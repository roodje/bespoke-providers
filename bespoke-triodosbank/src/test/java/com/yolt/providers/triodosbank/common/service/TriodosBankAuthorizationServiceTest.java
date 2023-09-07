package com.yolt.providers.triodosbank.common.service;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.triodosbank.common.model.http.ConsentStatusResponse;
import com.yolt.providers.triodosbank.common.rest.TriodosBankHttpClient;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriodosBankAuthorizationServiceTest {

    @Mock
    private TriodosBankHttpClient httpClient;

    private final TriodosBankAuthorizationService service = new TriodosBankAuthorizationService(Clock.systemUTC());

    @Test
    void shouldValidateConsentStatusValid() {
        // given
        ConsentStatusResponse expectedStatus = new ConsentStatusResponse();
        expectedStatus.setConsentStatus("valid");
        String consentId = "someConsentID";
        when(httpClient.getConsentStatus(consentId))
                .thenReturn(expectedStatus);
        // when
        assertThatCode(() -> service.validateConsentStatus(httpClient, consentId))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldValidateConsentStatusReceived() {
        // given
        ConsentStatusResponse expectedStatus = new ConsentStatusResponse();
        expectedStatus.setConsentStatus("received");
        String consentId = "someConsentID";
        when(httpClient.getConsentStatus(consentId))
                .thenReturn(expectedStatus);
        // when
        assertThatCode(() -> service.validateConsentStatus(httpClient, consentId))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionForNotCorrectConsentStatus() {
        // given
        ConsentStatusResponse expectedStatus = new ConsentStatusResponse();
        expectedStatus.setConsentStatus("rejected");
        String consentId = "someConsentID";
        when(httpClient.getConsentStatus(consentId))
                .thenReturn(expectedStatus);
        // when
        ThrowableAssert.ThrowingCallable exception = () -> service.validateConsentStatus(httpClient, consentId);

        // then
        assertThatThrownBy(exception)
                .isInstanceOf(GetAccessTokenFailedException.class)
                .hasMessage("Consent is not valid for getting data. Consent status: rejected");
    }

    @Test
    void shouldThrowExceptionWhenReceivingHttpStatusCodeExceptionForConsentConfirmation() {
        // given
        String consentId = "someConsentID";
        HttpStatusCodeException httpStatusCodeException = mock(HttpStatusCodeException.class);
        when(httpStatusCodeException.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(httpClient.getConsentStatus(consentId)).thenThrow(httpStatusCodeException);

        // when
        ThrowableAssert.ThrowingCallable exception = () -> service.validateConsentStatus(httpClient, consentId);

        // then
        assertThatThrownBy(exception)
                .isInstanceOf(GetAccessTokenFailedException.class)
                .hasMessage("Something went wrong on getting consent status verification: HTTP 400 BAD_REQUEST");
    }
}