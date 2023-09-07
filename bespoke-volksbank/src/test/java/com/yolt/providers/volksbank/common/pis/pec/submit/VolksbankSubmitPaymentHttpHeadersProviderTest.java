package com.yolt.providers.volksbank.common.pis.pec.submit;

import com.yolt.providers.volksbank.VolksbankSampleTypedAuthenticationMeans;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
import com.yolt.providers.volksbank.common.pis.pec.VolksbankPisHttpHeadersFactory;
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
class VolksbankSubmitPaymentHttpHeadersProviderTest {

    @InjectMocks
    private VolksbankSubmitPaymentHttpHeadersProvider subject;

    @Mock
    private VolksbankPisHttpHeadersFactory httpHeadersFactory;

    @Test
    void shouldReturnProperHttpHeadersForProvideHttpHeadersWhenCorrectData() {
        // given
        var preExecutionResult = preparePreExecutionResult();
        var expectedHttpHeaders = new HttpHeaders();

        given(httpHeadersFactory.createCommonHttpHeaders(anyString()))
                .willReturn(expectedHttpHeaders);

        // when
        var result = subject.provideHttpHeaders(preExecutionResult, null);

        // then
        then(httpHeadersFactory)
                .should()
                .createCommonHttpHeaders("someClientId");
        assertThat(result).isEqualTo(expectedHttpHeaders);
    }

    private VolksbankSepaSubmitPreExecutionResult preparePreExecutionResult() {
        return new VolksbankSepaSubmitPreExecutionResult(
                VolksbankAuthenticationMeans.fromAuthenticationMeans(new VolksbankSampleTypedAuthenticationMeans().getAuthenticationMeans(), "VOLKSBANK"),
                null,
                ""
        );
    }
}