package com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.MalformedDataInitiationException;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduled2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsentResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsentResponse5Data;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GenericInitiateScheduledPaymentProviderStateExtractorTest {

    @InjectMocks
    private GenericInitiateScheduledPaymentProviderStateExtractor subject;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnUkProviderStateWhenCorrectDataAreProvided() throws JsonProcessingException {
        // given
        OBWriteDomesticScheduled2DataInitiation dataInitiation = new OBWriteDomesticScheduled2DataInitiation();
        OBWriteDomesticScheduledConsentResponse5 obWriteDomesticScheduledConsentResponse5 = new OBWriteDomesticScheduledConsentResponse5()
                .data(new OBWriteDomesticScheduledConsentResponse5Data()
                        .consentId("consentId")
                        .initiation(dataInitiation));

        given(objectMapper.writeValueAsString(dataInitiation))
                .willReturn("dataInitiation");

        UkProviderState expectedState = new UkProviderState("consentId", PaymentType.SCHEDULED, "dataInitiation");

        // when
        UkProviderState result = subject.extractUkProviderState(obWriteDomesticScheduledConsentResponse5, null);

        // then
        assertThat(result).isEqualTo(expectedState);
    }

    @Test
    void shouldThrowMalformedDataInitiationExceptionWhenCannotSerializeDataInitiation() throws JsonProcessingException {
        // given
        OBWriteDomesticScheduled2DataInitiation dataInitiation = new OBWriteDomesticScheduled2DataInitiation();
        OBWriteDomesticScheduledConsentResponse5 obWriteDomesticScheduledConsentResponse5 = new OBWriteDomesticScheduledConsentResponse5()
                .data(new OBWriteDomesticScheduledConsentResponse5Data()
                        .consentId("consentId")
                        .initiation(dataInitiation));

        given(objectMapper.writeValueAsString(dataInitiation))
                .willThrow(JsonProcessingException.class);

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.extractUkProviderState(obWriteDomesticScheduledConsentResponse5, null);

        // then
        assertThatExceptionOfType(MalformedDataInitiationException.class)
                .isThrownBy(callable)
                .withMessage("Data initiation object cannot be parsed into JSON");
    }
}