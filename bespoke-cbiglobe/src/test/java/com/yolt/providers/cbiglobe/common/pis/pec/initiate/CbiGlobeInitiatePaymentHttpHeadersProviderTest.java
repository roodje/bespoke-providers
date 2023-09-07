package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.yolt.providers.cbiglobe.CbiGlobeSampleTypedAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.pis.pec.CbiGlobePisHttpHeadersFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CbiGlobeInitiatePaymentHttpHeadersProviderTest {

    @InjectMocks
    private CbiGlobeInitiatePaymentHttpHeadersProvider subject;

    @Mock
    private CbiGlobePisHttpHeadersFactory pisHttpHeadersFactory;

    @Test
    void shouldReturnProperHttpHeadersForProvideHttpHeadersWhenCorrectData() throws IOException, URISyntaxException {
        // given
        var preExecutionResult = preparePreExecutionResult();
        var expectedHttpHeaders = new HttpHeaders();

        given(pisHttpHeadersFactory.createPaymentInitiationHttpHeaders(any(), any(), any(), any(), any(), any(), any()))
                .willReturn(expectedHttpHeaders);

        // when
        var result = subject.provideHttpHeaders(preExecutionResult, null);

        // then
        then(pisHttpHeadersFactory)
                .should()
                .createPaymentInitiationHttpHeaders(
                        "",
                        null,
                        null,
                        "fakeAddress",
                        "",
                        null,
                        null
                );
        assertThat(result).isEqualTo(expectedHttpHeaders);
    }

    private CbiGlobeSepaInitiatePreExecutionResult preparePreExecutionResult() throws IOException, URISyntaxException {
        return new CbiGlobeSepaInitiatePreExecutionResult(
                null,
                CbiGlobeAuthenticationMeans.getCbiGlobeAuthenticationMeans(
                        new CbiGlobeSampleTypedAuthenticationMeans().getAuthenticationMeans(),
                        "CBI_GLOBE"),
                null,
                "fakeAddress",
                "",
                null,
                null,
                ""
        );
    }
}