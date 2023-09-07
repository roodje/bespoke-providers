package com.yolt.providers.abnamrogroup.common.pis.pec.submit;

import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.auth.AccessTokenResponseDTO;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroTestPisAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentCommonHttpHeadersProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AbnAmroSubmitPaymentHttpHeadersProviderTest {

    @InjectMocks
    private AbnAmroSubmitPaymentHttpHeadersProvider subject;

    @Mock
    private AbnAmroPaymentCommonHttpHeadersProvider httpHeadersProvider;

    @Test
    void shouldReturnHttpHeadersForProvideHttpHeadersWhenCorrectData() {
        // given
        AbnAmroSubmitPaymentPreExecutionResult preExecutionResult = new AbnAmroSubmitPaymentPreExecutionResult(
                new AccessTokenResponseDTO("accessToken", null, 0, "", ""),
                new AbnAmroAuthenticationMeans(new AbnAmroTestPisAuthenticationMeans().getAuthMeans()),
                null,
                null);
        HttpHeaders httpHeaders = new HttpHeaders();

        given(httpHeadersProvider.provideCommonHttpHeaders(anyString(), anyString()))
                .willReturn(httpHeaders);

        // when
        HttpHeaders result = subject.provideHttpHeaders(preExecutionResult, null);

        // then
        then(httpHeadersProvider)
                .should()
                .provideCommonHttpHeaders("accessToken", "7zacIF8Cu5o3XF4gUll4sRGuI2gDYiCA");
        assertThat(result).isEqualTo(httpHeaders);
    }
}