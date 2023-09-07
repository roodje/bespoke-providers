package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.yolt.providers.abnamrogroup.common.pis.AbnAmroXRequestIdHeaderProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AbnAmroPaymentCommonHttpHeadersProviderTest {

    @InjectMocks
    private AbnAmroPaymentCommonHttpHeadersProvider subject;

    @Mock
    private AbnAmroXRequestIdHeaderProvider xRequestIdHeaderProvider;

    @Test
    void shouldReturnHttpHeadersForProvideCommonHttpHeadersWhenCorrectData() {
        // given
        given(xRequestIdHeaderProvider.provideXRequestIdHeader())
                .willReturn("requestId");

        // when
        HttpHeaders result = subject.provideCommonHttpHeaders("accessToken", "apiKey");


        // then
        then(xRequestIdHeaderProvider)
                .should()
                .provideXRequestIdHeader();

        assertThat(result.toSingleValueMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE,
                        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                        HttpHeaders.AUTHORIZATION, "Bearer accessToken",
                        "API-Key", "apiKey",
                        "X-Request-ID", "requestId"
                ));
    }
}