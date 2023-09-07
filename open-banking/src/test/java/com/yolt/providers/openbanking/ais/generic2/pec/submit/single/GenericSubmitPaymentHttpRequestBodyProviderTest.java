package com.yolt.providers.openbanking.ais.generic2.pec.submit.single;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.MalformedDataInitiationException;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providershared.ProviderPayment;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GenericSubmitPaymentHttpRequestBodyProviderTest {

    private GenericSubmitPaymentHttpRequestBodyProvider subject;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach() {
        subject = new GenericSubmitPaymentHttpRequestBodyProvider(objectMapper, OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY);
    }

    @Test
    void shouldReturnWriteDomesticBasedOnUkProviderStateWhenProviderStateIsProvided() throws JsonProcessingException {
        // given
        GenericSubmitPaymentPreExecutionResult preExecutionResult = new GenericSubmitPaymentPreExecutionResult();
        preExecutionResult.setProviderState(prepareUkProviderState());
        OBWriteDomestic2DataInitiation dataInitiation = new OBWriteDomestic2DataInitiation();

        given(objectMapper.readValue(anyString(), eq(OBWriteDomestic2DataInitiation.class)))
                .willReturn(dataInitiation);

        // when
        OBWriteDomestic2 result = subject.provideHttpRequestBody(preExecutionResult);

        // then
        then(objectMapper)
                .should()
                .readValue("dataInitiation", OBWriteDomestic2DataInitiation.class);

        assertThat(result.getRisk()).satisfies(risk -> assertThat(risk.getPaymentContextCode()).isEqualTo(OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY));
        assertThat(result.getData()).satisfies(data -> {
            assertThat(data.getConsentId()).isEqualTo("consentId");
            assertThat(data.getInitiation()).isEqualTo(dataInitiation);
        });
    }

    @Test
    void shouldThrowMalformedDataInitiationExceptionWhenCannotParseDataInitiationJson() throws JsonProcessingException {
        // given
        GenericSubmitPaymentPreExecutionResult preExecutionResult = new GenericSubmitPaymentPreExecutionResult();
        preExecutionResult.setProviderState(prepareUkProviderState());

        given(objectMapper.readValue(anyString(), eq(OBWriteDomestic2DataInitiation.class)))
                .willThrow(JsonProcessingException.class);

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.provideHttpRequestBody(preExecutionResult);

        // then
        assertThatExceptionOfType(MalformedDataInitiationException.class)
                .isThrownBy(callable)
                .withMessage("Unable to parse data initiation");
    }

    private UkProviderState prepareUkProviderState() {
        return new UkProviderState("consentId", PaymentType.SINGLE, "dataInitiation");
    }

    private ProviderPayment prepareProviderPayment() {
        return new ProviderPayment(
                "",
                "",
                null,
                new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, ""),
                CurrencyCode.GBP,
                new BigDecimal("100.00"),
                "",
                ""
        );
    }
}