package com.yolt.providers.sparkassenandlandesbanks.common.service;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.sparkassenandlandesbanks.common.rest.SparkassenAndLandesbanksRestTemplateService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SparkassenAndLandesbanksAuthenticationServiceTest {

    @Mock
    private SparkassenAndLandesbanksRestTemplateService restTemplateService;

    private SparkassenAndLandesbanksAuthenticationService sparkassenAndLandesbanksAuthenticationService = new SparkassenAndLandesbanksAuthenticationService(restTemplateService);

    @Test
    void shouldThrowAnExceptionWhenThereIsErrorQueryParameter() {
        // given
        String redirectUrl = "https://yolt.com/callback?state=123&error=true";

        // when
        assertThatThrownBy(() -> sparkassenAndLandesbanksAuthenticationService.retrieveAuthCodeFromRedirectUrl(redirectUrl))
                .isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }
}