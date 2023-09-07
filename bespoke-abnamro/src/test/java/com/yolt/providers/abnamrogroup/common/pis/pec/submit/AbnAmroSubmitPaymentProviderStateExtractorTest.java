package com.yolt.providers.abnamrogroup.common.pis.pec.submit;

import com.yolt.providers.abnamrogroup.common.auth.AccessTokenResponseDTO;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentProviderState;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroProviderStateSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AbnAmroSubmitPaymentProviderStateExtractorTest {

    private AbnAmroSubmitPaymentProviderStateExtractor subject;

    @Mock
    private AbnAmroProviderStateSerializer providerStateSerializer;

    private final Clock clock = Clock.fixed(ZonedDateTime.parse("2021-07-02T12:00:00+02:00[Europe/Amsterdam]").toInstant(), ZoneId.of("Europe/Amsterdam"));

    @Captor
    private ArgumentCaptor<AbnAmroPaymentProviderState> providerStateArgumentCaptor;

    @BeforeEach
    void beforeEach() {
        this.subject = new AbnAmroSubmitPaymentProviderStateExtractor(providerStateSerializer, clock);
    }

    @Test
    void shouldReturnProviderStateAsStringTakenFromPreExecutionResultWhenCorrectDataProvided() {
        // given
        AbnAmroSubmitPaymentPreExecutionResult preExecutionResult = new AbnAmroSubmitPaymentPreExecutionResult(new AccessTokenResponseDTO("accessToken",
                "refreshToken",
                7200,
                "",
                ""),
                null,
                null,
                "transactionId");

        given(providerStateSerializer.serialize(any(AbnAmroPaymentProviderState.class)))
                .willReturn("providerState");

        // when
        String result = subject.extractProviderState(null, preExecutionResult);

        // then
        then(providerStateSerializer)
                .should()
                .serialize(providerStateArgumentCaptor.capture());
        AbnAmroPaymentProviderState capturedProviderState = providerStateArgumentCaptor.getValue();
        assertThat(capturedProviderState).satisfies(state -> {
            assertThat(state.getTransactionId()).isEqualTo("transactionId");
            assertThat(state.getRedirectUri()).isNull();
            assertThat(state.getUserAccessTokenState()).satisfies(userAccessTokenState -> {
                assertThat(userAccessTokenState.getAccessToken()).isEqualTo("accessToken");
                assertThat(userAccessTokenState.getRefreshToken()).isEqualTo("refreshToken");
                assertThat(userAccessTokenState.getExpirationZonedDateTime()).isEqualTo(ZonedDateTime.now(clock).plusSeconds(7200));
            });
        });
        assertThat(result).isEqualTo("providerState");
    }
}