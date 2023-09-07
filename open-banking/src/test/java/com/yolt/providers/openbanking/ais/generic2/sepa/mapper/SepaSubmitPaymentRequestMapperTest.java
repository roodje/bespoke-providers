package com.yolt.providers.openbanking.ais.generic2.sepa.mapper;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SepaSubmitPaymentRequestMapperTest {

    private SepaSubmitPaymentRequestMapper subject = new SepaSubmitPaymentRequestMapper();

    @Test
    void shouldMap() {
        //Given
        var signer = mock(Signer.class);
        var restTemplateManeger = mock(RestTemplateManager.class);
        var authenticationMeansReference = mock(AuthenticationMeansReference.class);
        var sepaRequest = new SubmitPaymentRequest(
                "providerState",
                new HashMap<>(),
                "https://redirecturi.com",
                signer,
                restTemplateManeger,
                "111.111.111.111",
                authenticationMeansReference);
        var expectedResult = new com.yolt.providers.common.pis.common.SubmitPaymentRequest(
                "providerState",
                new HashMap<>(),
                "https://redirecturi.com",
                signer,
                restTemplateManeger,
                "111.111.111.111",
                authenticationMeansReference);

        //When
        var result = subject.map(sepaRequest);

        //Then
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedResult);
    }
}