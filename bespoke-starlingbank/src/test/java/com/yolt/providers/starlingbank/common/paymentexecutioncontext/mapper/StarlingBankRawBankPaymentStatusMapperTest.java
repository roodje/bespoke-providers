package com.yolt.providers.starlingbank.common.paymentexecutioncontext.mapper;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class StarlingBankRawBankPaymentStatusMapperTest {

    @InjectMocks
    private StarlingBankRawBankPaymentStatusMapper rawBankPaymentStatusMapper;

    @Test
    void shouldExtractProviderState() {
        //given
        String rawResponseBody = "responseBody";

        //when
        RawBankPaymentStatus result = rawBankPaymentStatusMapper.mapBankPaymentStatus(rawResponseBody);

        //then
        assertThat(result)
                .extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                .contains("UNKNOWN", "");
    }
}
