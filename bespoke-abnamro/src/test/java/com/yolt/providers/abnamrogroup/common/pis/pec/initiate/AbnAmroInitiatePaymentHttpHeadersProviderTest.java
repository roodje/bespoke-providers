package com.yolt.providers.abnamrogroup.common.pis.pec.initiate;

import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroTestPisAuthenticationMeans;
import com.yolt.providers.abnamro.pis.SepaPayment;
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
class AbnAmroInitiatePaymentHttpHeadersProviderTest {

    @InjectMocks
    private AbnAmroInitiatePaymentHttpHeadersProvider subject;

    @Mock
    private AbnAmroPaymentCommonHttpHeadersProvider commonHttpHeadersProvider;

    @Test
    void shouldReturnHttpHeadersForProvideHttpHeadersWhenCorrectData() {
        // given
        AbnAmroInitiatePaymentPreExecutionResult preExecutionResult = createPreExecutionResult();
        HttpHeaders expectedHttpHeaders = new HttpHeaders();

        given(commonHttpHeadersProvider.provideCommonHttpHeaders(anyString(), anyString()))
                .willReturn(expectedHttpHeaders);

        // when
        HttpHeaders result = subject.provideHttpHeaders(preExecutionResult, new SepaPayment());

        // then
        then(commonHttpHeadersProvider)
                .should()
                .provideCommonHttpHeaders("fakeAccessToken", "7zacIF8Cu5o3XF4gUll4sRGuI2gDYiCA");

        assertThat(result).isEqualTo(expectedHttpHeaders);
    }

    private AbnAmroInitiatePaymentPreExecutionResult createPreExecutionResult() {
        return new AbnAmroInitiatePaymentPreExecutionResult(
                "fakeAccessToken",
                new AbnAmroAuthenticationMeans(new AbnAmroTestPisAuthenticationMeans().getAuthMeans()),
                null,
                null,
                null,
                null
        );
    }
}