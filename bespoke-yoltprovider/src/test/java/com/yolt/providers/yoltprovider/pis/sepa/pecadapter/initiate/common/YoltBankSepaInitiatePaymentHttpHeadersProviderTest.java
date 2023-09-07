package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common;

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
public class YoltBankSepaInitiatePaymentHttpHeadersProviderTest {

    @InjectMocks
    private YoltBankSepaInitiatePaymentHttpHeadersProvider httpHeadersProvider;

    @Mock
    private Signer signer;

    @Test
    void shouldReturnCorrectSetOfHttpHeadersForProvideHttpHeaders() {
        // given
        UUID clientId = UUID.randomUUID();
        UUID signingKid = UUID.randomUUID();
        UUID publicKid = UUID.randomUUID();
        YoltBankSepaInitiatePaymentPreExecutionResult preExecutionResult = new YoltBankSepaInitiatePaymentPreExecutionResult(
                null,
                new PaymentAuthenticationMeans(clientId, signingKid, publicKid),
                signer,
                "fakeRedirectUrl",
                "fakeState"
        );
        byte[] body = new byte[0];
        String emptyStringDigest = "SHA-256=47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=";
        byte[] signingStringBytes = String.format("clientId: %s, digest: %s", clientId, emptyStringDigest).getBytes();
        String fakeSignatureHeaderValue = String.format("\"keyId=\"%s\",algorithm=\"SHA256withRSA\",signature=\"fakeSignature\"\"", publicKid);
        given(signer.sign(eq(signingStringBytes), eq(signingKid), any(SignatureAlgorithm.class)))
                .willReturn("fakeSignature");

        // when
        HttpHeaders result = httpHeadersProvider.provideHttpHeaders(preExecutionResult, body);

        // then
        Map<String, String> httpHeaders = result.toSingleValueMap();
        assertThat(httpHeaders)
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "Content-Type", "application/json",
                        "client-id", clientId.toString(),
                        "digest", emptyStringDigest,
                        "signature", fakeSignatureHeaderValue,
                        "redirect_url", "fakeRedirectUrl?state=fakeState"
                ));
    }
}
