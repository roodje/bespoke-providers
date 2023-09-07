package com.yolt.providers.rabobank.pis.pec.submit;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.rabobank.RabobankAuthenticationMeans;
import com.yolt.providers.rabobank.RabobankSampleTypedAuthenticationMeans;
import com.yolt.providers.rabobank.pis.pec.RabobankCommonHttpHeaderProvider;
import com.yolt.providers.rabobank.pis.pec.RabobankPisHeadersSigner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.cert.CertificateEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabobankSepaSumbitPaymentHttpHeadersProviderTest {

    @InjectMocks
    private RabobankSepaSumbitPaymentHttpHeadersProvider subject;

    @Mock
    private RabobankCommonHttpHeaderProvider commonHttpHeaderProvider;

    @Mock
    private RabobankPisHeadersSigner pisHeadersSigner;

    @Mock
    private Signer signer;

    private RabobankSampleTypedAuthenticationMeans sampleTypedAuthenticationMeans = new RabobankSampleTypedAuthenticationMeans();

    @Test
    void shouldReturnFullHeaderSet() throws IOException, URISyntaxException, CertificateEncodingException {
        HttpHeaders httpCommonHeaders = new HttpHeaders();
        httpCommonHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        httpCommonHeaders.add("PSU-IP-Address", "127.0.0.1");
        httpCommonHeaders.add("x-request-id", "traceId");
        httpCommonHeaders.add("x-ibm-client-id", "0b7df55a-8f51-454e-9de8-87b9cac2aba4");
        httpCommonHeaders.add("date", "some-date");
        when(commonHttpHeaderProvider.providerCommonHttpHeaders("127.0.0.1", "0b7df55a-8f51-454e-9de8-87b9cac2aba4")).thenReturn(httpCommonHeaders);
        HttpHeaders headersWithRedirectUrls = new HttpHeaders(httpCommonHeaders);
        headersWithRedirectUrls.add("tpp-redirect-uri", "http://yolt.com/callback?state=123-456-789");
        headersWithRedirectUrls.add("tpp-nok-redirect-uri", "http://yolt.com/callback?state=123-456-789&error=denied");
        HttpHeaders expectedHeaders = new HttpHeaders(headersWithRedirectUrls);
        expectedHeaders.add("tpp-signature-certificate", "some-certificate");
        expectedHeaders.add("digest", "happy-digest");
        expectedHeaders.add("signature", "some-signature");
        byte[] bodyBytes = new byte[]{};
        RabobankAuthenticationMeans rabobankAuthenticationMeans = RabobankAuthenticationMeans.fromPISAuthenticationMeans(sampleTypedAuthenticationMeans.getRabobankSampleTypedAuthenticationMeans());
        RabobankSepaSubmitPaymentPreExecutionResult preExecutionResult = preparePreExecutionResult(rabobankAuthenticationMeans);
        when(pisHeadersSigner.signHeaders(headersWithRedirectUrls, bodyBytes, signer, rabobankAuthenticationMeans.getSigningKid(), rabobankAuthenticationMeans.getClientSigningCertificate())).thenReturn(expectedHeaders);

        //when
        HttpHeaders generatedHeaders = subject.provideHttpHeaders(preExecutionResult, null);

        //then
        assertThat(generatedHeaders).containsExactlyInAnyOrderEntriesOf(expectedHeaders);
    }

    private RabobankSepaSubmitPaymentPreExecutionResult preparePreExecutionResult(RabobankAuthenticationMeans rabobankAuthenticationMeans) {
        return new RabobankSepaSubmitPaymentPreExecutionResult(
                null,
                rabobankAuthenticationMeans,
                null,
                "127.0.0.1",
                signer);
    }

}
