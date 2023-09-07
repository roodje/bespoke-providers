package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import com.yolt.providers.yoltprovider.pis.TestPaymentAuthMeansUtil;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.SepaProviderState;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class YoltBankSepaSubmitPaymentPreExecutionResultMapperTest {

    @InjectMocks
    private YoltBankSepaSubmitPaymentPreExecutionResultMapper mapper;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnSepaSubmitPreExecutionResultForMap() throws JsonProcessingException {
        // given
        Signer signer = mock(Signer.class);
        UUID clientId = UUID.randomUUID();
        UUID privateKid = UUID.randomUUID();
        UUID publicKid = UUID.randomUUID();
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest(
                "fakeProviderState",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(clientId, publicKid, privateKid),
                null,
                signer,
                null,
                null,
                null
        );
        given(objectMapper.readValue("fakeProviderState", SepaProviderState.class))
                .willReturn(new SepaProviderState(
                        "fakePaymentId",
                        PaymentType.SINGLE
                ));

        // when
        YoltBankSepaSubmitPreExecutionResult result = mapper.map(submitPaymentRequest);

        // then

        assertThat(result).extracting(
                YoltBankSepaSubmitPreExecutionResult::getPaymentId,
                YoltBankSepaSubmitPreExecutionResult::getSigner
        ).contains("fakePaymentId", signer);
        assertThat(result.getAuthenticationMeans()).extracting(PaymentAuthenticationMeans::getClientId,
                PaymentAuthenticationMeans::getPublicKid,
                PaymentAuthenticationMeans::getSigningKid)
                .contains(clientId, publicKid, privateKid);
    }

    @Test
    void shouldThrowErrorWhenMappingFails() throws JsonProcessingException {
        //given
        Signer signer = mock(Signer.class);
        UUID clientId = UUID.randomUUID();
        UUID privateKid = UUID.randomUUID();
        UUID publicKid = UUID.randomUUID();
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest(
                "fakeProviderState",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(clientId, publicKid, privateKid),
                null,
                signer,
                null,
                null,
                null
        );
        doThrow(new JsonProcessingException("error"){}).when(objectMapper).readValue(anyString(), any(Class.class));
        //when
        ThrowableAssert.ThrowingCallable callable = () -> mapper.map(submitPaymentRequest);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not find payment type in providerState during payment submission");
    }
}
