package com.yolt.providers.volksbank.common.pis.pec.initiate;

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
class VolksbankInitiatePaymentHttpHeadersProviderV2Test {

    @InjectMocks
    private VolksbankInitiatePaymentHttpHeadersProviderV2 subject;

    @Mock
    private VolksbankPisHttpHeadersFactory pisHttpHeadersFactory;

    @Test
    void shouldReturnProperHttpHeadersForProvideHttpHeadersWhenCorrectData() {
        // given
        var preExecutionResult = preparePreExecutionResult();
        var expectedHttpHeaders = new HttpHeaders();

        given(pisHttpHeadersFactory.createPaymentInitiationHttpHeaders(anyString(), anyString()))
                .willReturn(expectedHttpHeaders);

        // when
        var result = subject.provideHttpHeaders(preExecutionResult, null);

        // then
        then(pisHttpHeadersFactory)
                .should()
                .createPaymentInitiationHttpHeaders("someClientId", "fakeAddress");
        assertThat(result).isEqualTo(expectedHttpHeaders);
    }

    private VolksbankSepaInitiatePreExecutionResult preparePreExecutionResult() {
        return new VolksbankSepaInitiatePreExecutionResult(
                null,
                VolksbankAuthenticationMeans.fromAuthenticationMeans(new VolksbankSampleTypedAuthenticationMeans().getAuthenticationMeans(), "VOLKSBANK"),
                null,
                "fakeAddress",
                "",
                ""
        );
    }
}