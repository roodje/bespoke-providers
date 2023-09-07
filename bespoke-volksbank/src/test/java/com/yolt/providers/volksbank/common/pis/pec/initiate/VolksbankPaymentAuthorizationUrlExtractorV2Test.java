package com.yolt.providers.volksbank.common.pis.pec.initiate;

import com.yolt.providers.volksbank.VolksbankSampleTypedAuthenticationMeans;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
import com.yolt.providers.volksbank.common.config.VolksbankBaseProperties;
import com.yolt.providers.volksbank.dto.v1_1.InitiatePaymentResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

import static com.yolt.providers.common.constants.OAuth.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class VolksbankPaymentAuthorizationUrlExtractorV2Test {

    @InjectMocks
    private VolksbankPaymentAuthorizationUrlExtractorV2 subject;

    @Mock
    private VolksbankBaseProperties properties;

    @Test
    void shouldReturnAuthorizationUrlForExtractAuthorizationUrlWhenCorrectData() {
        // given
        var initiatePaymentResponse = prepareInitiatePaymentResponse();
        var preExecutionResult = preparePreExecutionResult();

        given(properties.getAuthorizationUrl())
                .willReturn("http://localhost/authorize");

        // when
        var result = subject.extractAuthorizationUrl(initiatePaymentResponse, preExecutionResult);

        // then
        var uriComponents = UriComponentsBuilder.fromUriString(result).build();
        assertThat(uriComponents).extracting(UriComponents::getScheme, UriComponents::getHost, UriComponents::getPath)
                .contains("http", "localhost", "/authorize");
        assertThat(uriComponents.getQueryParams().toSingleValueMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        RESPONSE_TYPE, CODE,
                        SCOPE, "PIS",
                        STATE, "fakeState",
                        "paymentId", "fakePaymentId",
                        REDIRECT_URI, "fakeBaseClientRedirectUrl",
                        CLIENT_ID, "someClientId"
                ));
    }

    private InitiatePaymentResponse prepareInitiatePaymentResponse() {
        var initiatePaymentResponse = new InitiatePaymentResponse();
        initiatePaymentResponse.setPaymentId("fakePaymentId");
        return initiatePaymentResponse;
    }

    private VolksbankSepaInitiatePreExecutionResult preparePreExecutionResult() {
        return new VolksbankSepaInitiatePreExecutionResult(
                null,
                VolksbankAuthenticationMeans.fromAuthenticationMeans(new VolksbankSampleTypedAuthenticationMeans().getAuthenticationMeans(), "VOLKSBANK"),
                null,
                "",
                "fakeState",
                "fakeBaseClientRedirectUrl"
        );
    }
}