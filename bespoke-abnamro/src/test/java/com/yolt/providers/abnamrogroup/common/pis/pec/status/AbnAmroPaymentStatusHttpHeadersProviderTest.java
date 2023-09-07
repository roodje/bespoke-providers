package com.yolt.providers.abnamrogroup.common.pis.pec.status;

import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroTestPisAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentCommonHttpHeadersProvider;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentProviderState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AbnAmroPaymentStatusHttpHeadersProviderTest {

    @InjectMocks
    private AbnAmroPaymentStatusHttpHeadersProvider subject;

    @Mock
    private AbnAmroPaymentCommonHttpHeadersProvider commonHttpHeadersProvider;

    @Test
    void shouldReturnCommonPisHeadersWhenUserAccessTokenStateIsProvidedInProviderState() {
        // given
        AbnAmroPaymentStatusPreExecutionResult preExecutionResult = new AbnAmroPaymentStatusPreExecutionResult(new AbnAmroPaymentProviderState("",
                "",
                new AbnAmroPaymentProviderState.UserAccessTokenState("accessToken",
                        "",
                        0,
                        Clock.fixed(Instant.now(), ZoneId.of("UTC")))),
                new AbnAmroAuthenticationMeans(new AbnAmroTestPisAuthenticationMeans().getAuthMeans()),
                null);

        HttpHeaders expectedHeaders = new HttpHeaders();
        given(commonHttpHeadersProvider.provideCommonHttpHeaders(anyString(), anyString()))
                .willReturn(expectedHeaders);

        // when
        HttpHeaders result = subject.provideHttpHeaders(preExecutionResult, null);

        // then
        then(commonHttpHeadersProvider)
                .should()
                .provideCommonHttpHeaders("accessToken", "7zacIF8Cu5o3XF4gUll4sRGuI2gDYiCA");
        assertThat(result).isEqualTo(expectedHeaders);
    }
}