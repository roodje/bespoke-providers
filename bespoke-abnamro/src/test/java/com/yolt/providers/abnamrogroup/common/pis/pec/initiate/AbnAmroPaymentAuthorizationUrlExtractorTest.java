package com.yolt.providers.abnamrogroup.common.pis.pec.initiate;

import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.abnamro.AbnAmroProperties;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroTestPisAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.pis.InitiatePaymentResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AbnAmroPaymentAuthorizationUrlExtractorTest {

    @InjectMocks
    private AbnAmroPaymentAuthorizationUrlExtractor subject;

    @Mock
    private AbnAmroProperties properties;

    @Test
    void shouldReturnAuthorizationUrlForExtractAuthorizationUrlWhenCorrectData() {
        // given
        InitiatePaymentResponseDTO initiatePaymentResponseDTO = new InitiatePaymentResponseDTO(
                "", "transactionId", "", "", ""
        );
        AbnAmroInitiatePaymentPreExecutionResult preExecutionResult = new AbnAmroInitiatePaymentPreExecutionResult(
                "",
                new AbnAmroAuthenticationMeans(new AbnAmroTestPisAuthenticationMeans().getAuthMeans()),
                null,
                null,
                "baseClientRedirectUrl",
                "state"
        );

        given(properties.getOauth2Url())
                .willReturn("http://localhost/oauth2");

        // when
        String result = subject.extractAuthorizationUrl(initiatePaymentResponseDTO, preExecutionResult);

        // then
        then(properties)
                .should()
                .getOauth2Url();
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(result).build();
        assertThat(uriComponents).extracting(UriComponents::getScheme, UriComponents::getHost, UriComponents::getPath)
                .contains("http", "localhost", "/oauth2");
        assertThat(uriComponents.getQueryParams().toSingleValueMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "scope", "psd2:payment:sepa:write+psd2:payment:sepa:read",
                        "client_id", "TPP_test",
                        "transactionId", "transactionId",
                        "response_type", "code",
                        "flow", "code",
                        "redirect_uri", "baseClientRedirectUrl",
                        "state", "state"
                ));
    }
}