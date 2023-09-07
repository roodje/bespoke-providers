package com.yolt.providers.cbiglobe.common.pis.pec.submit;

import com.yolt.providers.cbiglobe.CbiGlobeSampleTypedAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.pis.pec.CbiGlobePisHttpHeadersFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CbiGlobeSubmitPaymentHttpHeadersProviderTest {

    @InjectMocks
    private CbiGlobeSubmitPaymentHttpHeadersProvider subject;

    @Mock
    private CbiGlobePisHttpHeadersFactory httpHeadersFactory;

    @Test
    void shouldReturnProperHttpHeadersForProvideHttpHeadersWhenCorrectData() {
        // given
        var preExecutionResult = preparePreExecutionResult();
        var expectedHttpHeaders = new HttpHeaders();

        given(httpHeadersFactory.createPaymentStatusHeaders(any(), any(), any(), any()))
                .willReturn(expectedHttpHeaders);

        // when
        var result = subject.provideHttpHeaders(preExecutionResult, null);

        // then
        then(httpHeadersFactory)
                .should()
                .createPaymentStatusHeaders("someAccessToken",
                        null,
                        null,
                        null
                );
        assertThat(result).isEqualTo(expectedHttpHeaders);
    }

    private CbiGlobeSepaSubmitPreExecutionResult preparePreExecutionResult() {
        return new CbiGlobeSepaSubmitPreExecutionResult(
                CbiGlobeAuthenticationMeans.getCbiGlobeAuthenticationMeans(
                        new CbiGlobeSampleTypedAuthenticationMeans().getAuthenticationMeans(),
                        "CBI_GLOBE"),
                null,
                "",
                "someAccessToken",
                null,
                null
        );
    }
}