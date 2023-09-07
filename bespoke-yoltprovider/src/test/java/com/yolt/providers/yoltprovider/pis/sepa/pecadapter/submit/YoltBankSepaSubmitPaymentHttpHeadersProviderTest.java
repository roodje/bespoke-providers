package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class YoltBankSepaSubmitPaymentHttpHeadersProviderTest {

    @InjectMocks
    private YoltBankSepaSubmitPaymentHttpHeadersProvider httpHeadersProvider;

    @Mock
    private Signer signer;

    @Test
    void shouldReturnProperHttpHeadersForProvideHttpHeaders() {
        // given
        UUID clientId = UUID.randomUUID();
        UUID signingKid = UUID.randomUUID();
        UUID publicKid = UUID.randomUUID();
        YoltBankSepaSubmitPreExecutionResult preExecutionResult = new YoltBankSepaSubmitPreExecutionResult(
                "fakePaymentId",
                new PaymentAuthenticationMeans(clientId, signingKid, publicKid),
                signer,
                null
        );
        String fakePaymentIdDigest = "SHA-256=oCqDdZCxDlVi+TvIm6K65na+P4c+2DiB+D2Lc95Z/vs=";
        byte[] signingStringBytes = String.format("clientId: %s, digest: %s", clientId, fakePaymentIdDigest).getBytes();
        String fakeSignatureHeaderValue = String.format("\"keyId=\"%s\",algorithm=\"SHA256withRSA\",signature=\"fakeSignature\"\"", publicKid);
        given(signer.sign(eq(signingStringBytes), eq(signingKid), any(SignatureAlgorithm.class)))
                .willReturn("fakeSignature");

        // when
        HttpHeaders result = httpHeadersProvider.provideHttpHeaders(preExecutionResult, null);

        // then
        Map<String, String> httpHeaders = result.toSingleValueMap();
        assertThat(httpHeaders)
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "Content-Type", "application/json",
                        "client-id", clientId.toString(),
                        "digest", fakePaymentIdDigest,
                        "signature", fakeSignatureHeaderValue
                ));
    }
}
