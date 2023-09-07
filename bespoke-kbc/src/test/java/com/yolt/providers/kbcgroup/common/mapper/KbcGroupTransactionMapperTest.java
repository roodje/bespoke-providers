package com.yolt.providers.kbcgroup.common.mapper;

import com.yolt.providers.kbcgroup.dto.Amount1;
import com.yolt.providers.kbcgroup.dto.Transaction1;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KbcGroupTransactionMapperTest {

    @Test
    public void shouldMapRemittanceInformationWhenMappingTransactionsWhenTheyAreNull() {
        //given
        Transaction1 sampleTransaction = new Transaction1();
        sampleTransaction.setTransactionId("sampleId");
        Amount1 sampleAmount = new Amount1();
        sampleAmount.setAmount("1.29");
        sampleTransaction.setTransactionAmount(sampleAmount);

        //and
        sampleTransaction.setRemittanceInformationUnstructured(null);

        //when
        ProviderTransactionDTO result = KbcGroupTransactionMapper.toProviderTransactionDto(sampleTransaction);

        //then
        assertEquals(result.getDescription(), "");
    }

    @Test
    public void shouldMapRemittanceInformationWhenMappingTransactionsWhenTheyArePresent() {
        //given
        Transaction1 sampleTransaction = new Transaction1();
        sampleTransaction.setTransactionId("sampleId");
        Amount1 sampleAmount = new Amount1();
        sampleAmount.setAmount("1.29");
        sampleTransaction.setTransactionAmount(sampleAmount);
        String sampleRemittanceInforamtion = "Payment with CBC Debit Card via Bancontact";

        //and
        sampleTransaction.setRemittanceInformationUnstructured(sampleRemittanceInforamtion);

        //when
        ProviderTransactionDTO result = KbcGroupTransactionMapper.toProviderTransactionDto(sampleTransaction);

        //then
        assertEquals(result.getDescription(), sampleRemittanceInforamtion);
    }
}
