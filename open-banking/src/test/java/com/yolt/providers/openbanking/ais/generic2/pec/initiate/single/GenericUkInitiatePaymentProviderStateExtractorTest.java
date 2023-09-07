package com.yolt.providers.openbanking.ais.generic2.pec.initiate.single;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.MalformedDataInitiationException;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsentResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsentResponse5Data;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GenericUkInitiatePaymentProviderStateExtractorTest {

    @InjectMocks
    private GenericInitiatePaymentProviderStateExtractor subject;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnUkProviderStateWhenCorrectDataAreProvided() throws JsonProcessingException {
        // given
        OBWriteDomestic2DataInitiation dataInitiation = new OBWriteDomestic2DataInitiation();
        OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 = new OBWriteDomesticConsentResponse5()
                .data(new OBWriteDomesticConsentResponse5Data()
                        .consentId("consentId")
                        .initiation(dataInitiation));

        given(objectMapper.writeValueAsString(any(OBWriteDomestic2DataInitiation.class)))
                .willReturn("dataInitiation");

        // when
        UkProviderState result = subject.extractUkProviderState(obWriteDomesticConsentResponse5, null);

        // then
        then(objectMapper)
                .should()
                .writeValueAsString(dataInitiation);

        assertThat(result.getConsentId()).isEqualTo("consentId");
        assertThat(result.getPaymentType()).isEqualTo(PaymentType.SINGLE);
        assertThat(result.getOpenBankingPayment()).isEqualTo("dataInitiation");
    }

    @Test
    void shouldThrowMalformedDataInitiationExceptionWhenCannotSerializeDataInitiation() throws JsonProcessingException {
        // given
        OBWriteDomestic2DataInitiation dataInitiation = new OBWriteDomestic2DataInitiation();
        OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 = new OBWriteDomesticConsentResponse5()
                .data(new OBWriteDomesticConsentResponse5Data()
                        .consentId("consentId")
                        .initiation(dataInitiation));

        given(objectMapper.writeValueAsString(any(OBWriteDomestic2DataInitiation.class)))
                .willThrow(JsonProcessingException.class);

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.extractUkProviderState(obWriteDomesticConsentResponse5, null);

        // then
        assertThatExceptionOfType(MalformedDataInitiationException.class)
                .isThrownBy(callable)
                .withMessage("Data initiation object cannot be parsed into JSON");
    }
}