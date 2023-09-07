package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.DefaultAccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.amount.DefaultAmountParser;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount.DefaultBalanceAmountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.currency.DefaultCurrencyMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedtransaction.DefaultExtendedTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.period.DefaultPeriodMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.DefaultSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.time.DefaultDateTimeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.DefaultTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactiontype.DefaultTransactionTypeMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBActiveOrHistoricCurrencyAndAmount3;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBActiveOrHistoricCurrencyAndAmount9;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBStandingOrder6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBTransaction6;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultTransactionMapperTest {
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/London");

    private final DefaultExtendedTransactionMapper extendedTransactionMapper = new DefaultExtendedTransactionMapper(
            new DefaultAccountReferenceTypeMapper(),
            new DefaultTransactionStatusMapper(),
            new DefaultBalanceAmountMapper(new DefaultCurrencyMapper(), new DefaultBalanceMapper()),
            false,
            DEFAULT_ZONE);

    private final DefaultTransactionMapper transactionMapper = new DefaultTransactionMapper(
            extendedTransactionMapper,
            new DefaultDateTimeMapper(ZoneId.of("Europe/London")),
            new DefaultTransactionStatusMapper(),
            new DefaultAmountParser(),
            new DefaultTransactionTypeMapper());
    private final DefaultStandingOrderMapper standingOrderMapper = new DefaultStandingOrderMapper(new DefaultPeriodMapper(), new DefaultAmountParser(), new DefaultSchemeMapper(), new DefaultDateTimeMapper(ZoneId.of("Europe/London")));

    @Test
    public void shouldReturnValidStandingOrderForMapToStandingOrderWhenInvalidFrequencyProvided() {
        // given
        OBStandingOrder6 standingOrderExample = getExampleOBStandingOrderWithSpecificFrequency("SIXTH DAY OF MONTH");

        // when

        StandingOrderDTO standingOrderDTO = standingOrderMapper.apply(standingOrderExample);

        // then
        assertThat(standingOrderDTO.getStandingOrderId()).isEqualTo("123");
        assertThat(standingOrderDTO.getNextPaymentAmount()).isEqualTo("0.50");
        assertThat(standingOrderDTO.getFrequency()).isEqualTo(Period.ZERO);
    }

    @Test
    public void shouldReturnValidStandingOrderForMapToStandingOrderWhenNullFrequencyProvided() {
        // given
        OBStandingOrder6 standingOrderExample = getExampleOBStandingOrderWithSpecificFrequency(null);

        // when
        StandingOrderDTO standingOrderDTO = standingOrderMapper.apply(standingOrderExample);

        // then
        assertThat(standingOrderDTO.getStandingOrderId()).isEqualTo("123");
        assertThat(standingOrderDTO.getNextPaymentAmount()).isEqualTo("0.50");
        assertThat(standingOrderDTO.getFrequency()).isEqualTo(Period.ZERO);
    }

    @Test
    public void shouldReturnValidDateTimeZones() {
        //given
        OBTransaction6 transactionSummer = getExampleOBTransactionSummerDate();
        OBTransaction6 transactionWinter = getExampleOBTransactionWinterDate();
        ZonedDateTime dateTimeSummer = ZonedDateTime.of(2020, 6, 30, 23, 0, 0, 0, DEFAULT_ZONE);
        ZonedDateTime dateTimeWinter = ZonedDateTime.of(2019, 12, 31, 22, 0, 0, 0, DEFAULT_ZONE);

        //when
        ProviderTransactionDTO transactionDTOSummer = transactionMapper.apply(transactionSummer);
        ProviderTransactionDTO transactionDTOWinter = transactionMapper.apply(transactionWinter);

        //then
        assertThat(transactionDTOSummer.getDateTime().getZone()).isEqualTo(DEFAULT_ZONE);
        assertThat(transactionDTOSummer.getDateTime()).isEqualTo(dateTimeSummer);
        assertThat(transactionDTOSummer.getExtendedTransaction().getBookingDate().getZone()).isEqualTo(DEFAULT_ZONE);
        assertThat(transactionDTOSummer.getExtendedTransaction().getValueDate().getZone()).isEqualTo(DEFAULT_ZONE);
        assertThat(transactionDTOWinter.getDateTime().getZone()).isEqualTo(DEFAULT_ZONE);
        assertThat(transactionDTOWinter.getDateTime()).isEqualTo(dateTimeWinter);
        assertThat(transactionDTOWinter.getExtendedTransaction().getBookingDate().getZone()).isEqualTo(DEFAULT_ZONE);
        assertThat(transactionDTOWinter.getExtendedTransaction().getValueDate().getZone()).isEqualTo(DEFAULT_ZONE);
    }

    private OBStandingOrder6 getExampleOBStandingOrderWithSpecificFrequency(String specificFrequency) {
        OBActiveOrHistoricCurrencyAndAmount3 nextPaymentAmount = new OBActiveOrHistoricCurrencyAndAmount3();
        nextPaymentAmount.setAmount("0.50");
        nextPaymentAmount.setCurrency("GBP");
        OBStandingOrder6 obStandingOrder = new OBStandingOrder6();
        obStandingOrder.setStandingOrderId("123");
        obStandingOrder.setNextPaymentAmount(nextPaymentAmount);
        obStandingOrder.setFrequency(specificFrequency);
        return obStandingOrder;
    }

    private OBTransaction6 getExampleOBTransactionSummerDate() {
        OBTransaction6 obTransaction = new OBTransaction6();
        obTransaction.setBookingDateTime(OffsetDateTime.of(2020, 7, 1, 0, 0, 0, 0, ZoneOffset.ofHours(2)).toString());
        obTransaction.setValueDateTime(OffsetDateTime.of(2020, 7, 1, 0, 0, 0, 0, ZoneOffset.ofHours(2)).toString());
        obTransaction.setAmount(new OBActiveOrHistoricCurrencyAndAmount9());
        return obTransaction;
    }

    private OBTransaction6 getExampleOBTransactionWinterDate() {
        OBTransaction6 obTransaction = new OBTransaction6();
        obTransaction.setBookingDateTime(OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(2)).toString());
        obTransaction.setValueDateTime(OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(2)).toString());
        obTransaction.setAmount(new OBActiveOrHistoricCurrencyAndAmount9());
        return obTransaction;
    }
}
