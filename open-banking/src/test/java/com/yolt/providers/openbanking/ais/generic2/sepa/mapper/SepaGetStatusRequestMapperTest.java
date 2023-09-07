package com.yolt.providers.openbanking.ais.generic2.sepa.mapper;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SepaGetStatusRequestMapperTest {

    private final SepaGetStatusRequestMapper subject = new SepaGetStatusRequestMapper();
    private final Signer signer = mock(Signer.class);
    private final RestTemplateManager restTemplateManager = mock(RestTemplateManager.class);

    @Test
    void shouldMap() {
        //Given
        var authMeansRef = mock(AuthenticationMeansReference.class);
        var sepaRequest = new GetStatusRequest(
                "providerState",
                "paymentId",
                new HashMap<>(),
                signer,
                restTemplateManager,
                "111.111.111.111",
                authMeansRef
        );

        var expectedResult = new com.yolt.providers.common.pis.common.GetStatusRequest(
                "providerState",
                "paymentId",
                new HashMap<>(),
                signer,
                restTemplateManager,
                "111.111.111.111",
                authMeansRef
        );

        //When
        var result = subject.map(sepaRequest);

        //Then
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedResult);
    }
}