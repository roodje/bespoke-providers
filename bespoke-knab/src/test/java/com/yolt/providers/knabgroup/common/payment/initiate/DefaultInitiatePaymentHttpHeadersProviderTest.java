package com.yolt.providers.knabgroup.common.payment.initiate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans;
import com.yolt.providers.knabgroup.common.auth.SignatureData;
import com.yolt.providers.knabgroup.common.payment.DefaultCommonPaymentHttpHeadersProvider;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.InitiatePaymentPreExecutionResult;
import com.yolt.providers.knabgroup.common.payment.dto.external.InitiatePaymentRequestBody;
import com.yolt.providers.knabgroup.samples.SampleAuthenticationMeans;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DefaultInitiatePaymentHttpHeadersProviderTest {

    @InjectMocks
    private DefaultInitiatePaymentExecutionHttpHeadersProvider subject;

    @Mock
    private DefaultCommonPaymentHttpHeadersProvider headersProvider;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    public void shouldReturnHttpHeaderForCorrectData() throws JsonProcessingException {
        // given
        InitiatePaymentPreExecutionResult preExecutionResult = createPreExecutionResult();
        HttpHeaders expectedHeaders = createExpectedHeaders();

        given(objectMapper.writeValueAsString(any(InitiatePaymentRequestBody.class)))
                .willReturn("");
        given(headersProvider.provideHttpHeaders(anyString(), any(SignatureData.class), any(byte[].class), anyString()))
                .willReturn(new HttpHeaders());

        // when
        HttpHeaders result = subject.provideHttpHeaders(preExecutionResult, sampleRequestBody());

        // then
        Assertions.assertThat(result).isEqualTo(expectedHeaders);
    }

    private InitiatePaymentRequestBody sampleRequestBody() {
        return new InitiatePaymentRequestBody(
                new InitiatePaymentRequestBody.BankAccount("iban"),
                new InitiatePaymentRequestBody.BankAccount("iban"),
                new InitiatePaymentRequestBody.InstructedAmount("31.22", "EUR"),
                "creditorName",
                "remittanceInformationUnstructured"
        );
    }

    private HttpHeaders createExpectedHeaders() {
        var headers = new HttpHeaders();
        headers.add("TPP-Redirect-URI", "https://localhost.com?state=fakeState");
        return headers;
    }

    private InitiatePaymentPreExecutionResult createPreExecutionResult() {
        return new InitiatePaymentPreExecutionResult(
                null,
                null,
                KnabGroupAuthenticationMeans.createKnabGroupAuthenticationMeans(SampleAuthenticationMeans.getSampleAuthenticationMeans(), "providers"),
                "accessToken",
                mock(Signer.class),
                "https://localhost.com",
                "fakeState",
                "fakeIp"
        );
    }
}