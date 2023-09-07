package com.yolt.providers.rabobank.pis.pec.initiate;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.rabobank.RabobankAuthenticationMeans;
import com.yolt.providers.rabobank.RabobankSampleTypedAuthenticationMeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class RabobankSepaInitiatePreExecutionResultMapperTest {

    private RabobankSepaInitiatePreExecutionResultMapper subject;

    private RabobankSampleTypedAuthenticationMeans sampleTypedAuthenticationMeans = new RabobankSampleTypedAuthenticationMeans();

    @BeforeEach
    void setUp() {
        subject = new RabobankSepaInitiatePreExecutionResultMapper();
    }

    @Test
    void shouldReturnProperlyMappedInitiatePaymentRequest() throws IOException, URISyntaxException {
        //given
        RabobankAuthenticationMeans authenticationMeans = RabobankAuthenticationMeans.fromPISAuthenticationMeans(sampleTypedAuthenticationMeans.getRabobankSampleTypedAuthenticationMeans());
        SepaInitiatePaymentRequestDTO requestDto = mock(SepaInitiatePaymentRequestDTO.class);
        Signer signer = mock(Signer.class);
        RestTemplateManager restTemplateManager = mock(RestTemplateManager.class);
        RabobankSepaInitiatePreExecutionResult expectedPreExecutionResult = new RabobankSepaInitiatePreExecutionResult(authenticationMeans,
                requestDto,
                "http://redirectUrl.com",
                signer,
                "127.0.0.1",
                restTemplateManager,
                "123-456-999");
        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequest(
                requestDto,
                "http://redirectUrl.com",
                "123-456-999",
                sampleTypedAuthenticationMeans.getRabobankSampleTypedAuthenticationMeans(),
                signer,
                restTemplateManager,
                "127.0.0.1",
                null);

        //when
        RabobankSepaInitiatePreExecutionResult preExecutionResult = subject.map(initiatePaymentRequest);

        //then
        assertThat(preExecutionResult).isEqualTo(expectedPreExecutionResult);
    }
}
