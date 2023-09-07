package com.yolt.providers.openbanking.ais.generic2.signer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.JwsSigningResult;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsent4;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.HeaderParameterNames;
import org.jose4j.jwx.JsonWebStructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExternalPaymentNoB64RequestSignerTest {

    private PaymentRequestSigner sut;

    @Mock
    private Signer signer;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    public void beforeEach() throws JsonProcessingException {
        sut = new ExternalPaymentNoB64RequestSigner(objectMapper, SignatureAlgorithm.SHA256_WITH_RSA_PSS.getJsonSignatureAlgorithm());

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"test\":\"test\"}");
        JwsSigningResult signingResult = mock(JwsSigningResult.class);
        when(signingResult.getDetachedContentCompactSerialization())
                .thenReturn("fakeSignature");
        when(signer.sign(any(JsonWebSignature.class), any(UUID.class), any(SignatureAlgorithm.class)))
                .thenReturn(signingResult);
    }

    @Test
    public void shouldReturnSignatureForCorrectlyPreparedJwsForCreateRequestSignatureWithCorrectData() throws JsonProcessingException {
        // given
        OBWriteDomesticConsent4 createPaymentRequest = new OBWriteDomesticConsent4();
        UUID signingPrivateKeyId = UUID.fromString("99ec1699-4a7a-41c5-8e10-d7a217353b64");
        DefaultAuthMeans defaultAuthMeans = DefaultAuthMeans.builder()
                .organizationId("org1")
                .softwareId("soft1")
                .signingKeyIdHeader("key123")
                .signingPrivateKeyId(signingPrivateKeyId)
                .build();
        ArgumentCaptor<JsonWebSignature> jsonWebSignatureArgumentCaptor = ArgumentCaptor.forClass(JsonWebSignature.class);

        // when
        String result = sut.createRequestSignature(createPaymentRequest, defaultAuthMeans, signer);

        // then
        assertThat(result).isEqualTo("fakeSignature");
        verify(objectMapper).writeValueAsString(createPaymentRequest);
        verify(signer).sign(jsonWebSignatureArgumentCaptor.capture(), eq(signingPrivateKeyId), eq(SignatureAlgorithm.SHA256_WITH_RSA_PSS));
        JsonWebSignature capturedJws = jsonWebSignatureArgumentCaptor.getValue();
        assertThat(capturedJws).extracting(JsonWebStructure::getAlgorithmHeaderValue, JsonWebSignature::getUnverifiedPayload, JsonWebStructure::getKeyIdHeaderValue)
                .contains(SignatureAlgorithm.SHA256_WITH_RSA_PSS.getJsonSignatureAlgorithm(), "{\"test\":\"test\"}", "key123");
        assertThat(capturedJws.getHeaders()).satisfies(headers -> {
            assertThat(headers.getObjectHeaderValue(CRITICAL_HEADER_OPENBANKING_IAT)).isNotNull();
            assertThat(headers.getObjectHeaderValue(CRITICAL_HEADER_OPENBANKING_TAN)).isEqualTo(OPENBANKING_ORG_UK);
            assertThat(headers.getObjectHeaderValue(CRITICAL_HEADER_OPENBANKING_ISS)).isEqualTo("org1/soft1");
            assertThat(headers.getObjectHeaderValue("crit")).asList()
                    .contains(CRITICAL_HEADER_OPENBANKING_IAT,
                            CRITICAL_HEADER_OPENBANKING_ISS,
                            CRITICAL_HEADER_OPENBANKING_TAN);
        });
    }

    @Test
    public void shouldReturnSignatureForCorrectlyPreparedJwsWithoutB64ClaimForCreateRequestSignatureWithCorrectData() {
        // given
        OBWriteDomesticConsent4 createPaymentRequest = new OBWriteDomesticConsent4();
        UUID signingPrivateKeyId = UUID.fromString("99ec1699-4a7a-41c5-8e10-d7a217353b64");
        DefaultAuthMeans defaultAuthMeans = DefaultAuthMeans.builder()
                .organizationId("org1")
                .softwareId("soft1")
                .signingKeyIdHeader("key123")
                .signingPrivateKeyId(signingPrivateKeyId)
                .build();
        ArgumentCaptor<JsonWebSignature> jsonWebSignatureArgumentCaptor = ArgumentCaptor.forClass(JsonWebSignature.class);

        // when
        sut.createRequestSignature(createPaymentRequest, defaultAuthMeans, signer);

        // then
        verify(signer).sign(jsonWebSignatureArgumentCaptor.capture(), eq(signingPrivateKeyId), eq(SignatureAlgorithm.SHA256_WITH_RSA_PSS));
        JsonWebSignature capturedJws = jsonWebSignatureArgumentCaptor.getValue();
        assertThat(capturedJws.getHeaders().getObjectHeaderValue(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD)).isNull();
        assertThat(capturedJws.getHeaders().getObjectHeaderValue("crit"))
                .asList()
                .doesNotContain("b64");
    }
}
