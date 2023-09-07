package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single;

import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InitiatePaymentConsentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class YoltBankUkSinglePaymentProviderStateExtractorTest {

    private YoltBankUkSinglePaymentProviderStateExtractor ukProviderStateExtractor;

    @BeforeEach
    public void setup() {
        ukProviderStateExtractor = new YoltBankUkSinglePaymentProviderStateExtractor();
    }

    @Test
    void shouldReturnUkProviderStateForExtractUkProviderState() {
        // given
        InitiatePaymentConsentResponse initiatePaymentConsentResponse = new InitiatePaymentConsentResponse(
                "",
                "fakePaymentConsent"
        );

        // when
        UkProviderState result = ukProviderStateExtractor.extractUkProviderState(initiatePaymentConsentResponse, null);

        // then
        assertThat(result).isEqualTo(new UkProviderState(null, PaymentType.SINGLE, null));
    }
}