package com.yolt.providers.monorepogroup.qontogroup.common.mapper;

import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Transaction;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providerdomain.YoltCategory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class DefaultQontoGroupTransactionMapperTest {

    private Clock clock = Clock.fixed(Instant.parse("2022-01-01T00:00:00Z"), ZoneId.of("UTC"));
    private ZoneId zoneId = ZoneId.of("Europe/Paris");
    private QontoGroupDateMapper dateMapper = new QontoGroupDateMapper(zoneId, clock);

    private DefaultQontoGroupTransactionMapper transactionMapper = new DefaultQontoGroupTransactionMapper(dateMapper);

    private static Stream<Arguments> getTransactionVariables() {
        return Stream.of(
                Arguments.of("credit", ProviderTransactionType.CREDIT, "completed", TransactionStatus.BOOKED),
                Arguments.of("debit", ProviderTransactionType.DEBIT, "pending", TransactionStatus.PENDING)
        );
    }


    @ParameterizedTest
    @MethodSource("getTransactionVariables")
    void shouldMapTransaction(String side, ProviderTransactionType transactionType, String status, TransactionStatus transactionStatus) {
        //given
        var returnedTransaction = mock(Transaction.class);
        given(returnedTransaction.getTransactionId()).willReturn("TRANS_ID123");
        given(returnedTransaction.getSettledAt()).willReturn(OffsetDateTime.of(2022, 03, 15, 07, 20, 00, 0, ZoneOffset.UTC));
        given(returnedTransaction.getEmittedAt()).willReturn(OffsetDateTime.of(2022, 03, 14, 16, 55, 00, 0, ZoneOffset.UTC));
        given(returnedTransaction.getSide()).willReturn(side);
        given(returnedTransaction.getAmount()).willReturn(new BigDecimal("22.88"));
        given(returnedTransaction.getCurrency()).willReturn("EUR");
        given(returnedTransaction.getReference()).willReturn("Some transaction reference");
        given(returnedTransaction.getLabel()).willReturn("Counterparty");
        given(returnedTransaction.getStatus()).willReturn(status);
        var mappedExtendedTransactionBuilder = ExtendedTransactionDTO.builder()
                .bookingDate(ZonedDateTime.of(2022, 03, 15, 8, 20, 00, 00, ZoneId.of("Europe/Paris")))
                .valueDate(ZonedDateTime.of(2022, 03, 14, 17, 55, 00, 00, ZoneId.of("Europe/Paris")))
                .status(transactionStatus)
                .transactionAmount(BalanceAmountDTO.builder()
                        .amount(ProviderTransactionType.DEBIT.equals(transactionType) ? new BigDecimal("22.88").negate() : new BigDecimal("22.88"))
                        .currency(CurrencyCode.EUR)
                        .build())
                .remittanceInformationUnstructured("Some transaction reference");
        if (ProviderTransactionType.DEBIT.equals(transactionType)) {
            mappedExtendedTransactionBuilder.creditorName("Counterparty");
        } else if (ProviderTransactionType.CREDIT.equals(transactionType)) {
            mappedExtendedTransactionBuilder.debtorName("Counterparty");
        }
        var expectedMappedTransaction = ProviderTransactionDTO.builder()
                .externalId("TRANS_ID123")
                .dateTime(ZonedDateTime.of(2022, 03, 15, 8, 20, 00, 00, ZoneId.of("Europe/Paris")))
                .type(transactionType)
                .category(YoltCategory.GENERAL)
                .amount(new BigDecimal("22.88"))
                .description("Some transaction reference")
                .status(transactionStatus)
                .extendedTransaction(mappedExtendedTransactionBuilder.build())
                .build();

        //when
        var result = transactionMapper.map(returnedTransaction);

        //then
        assertThat(result).isEqualTo(expectedMappedTransaction);
    }

    @ParameterizedTest
    @MethodSource("getTransactionVariables")
    void shouldMapTransactionWhenSomeFieldsAreMissing(String side, ProviderTransactionType transactionType, String status, TransactionStatus transactionStatus) {
        //given
        var returnedTransaction = mock(Transaction.class);
        given(returnedTransaction.getTransactionId()).willReturn("TRANS_ID123");
        given(returnedTransaction.getSide()).willReturn(side);
        given(returnedTransaction.getAmount()).willReturn(new BigDecimal("22.88"));
        given(returnedTransaction.getStatus()).willReturn(status);
        var mappedExtendedTransactionBuilder = ExtendedTransactionDTO.builder()
                .status(transactionStatus)
                .transactionAmount(BalanceAmountDTO.builder()
                        .amount(ProviderTransactionType.DEBIT.equals(transactionType) ? new BigDecimal("22.88").negate() : new BigDecimal("22.88"))
                        .build());
        var expectedMappedTransaction = ProviderTransactionDTO.builder()
                .externalId("TRANS_ID123")
                .type(transactionType)
                .category(YoltCategory.GENERAL)
                .amount(new BigDecimal("22.88"))
                .description("N/A")
                .status(transactionStatus)
                .extendedTransaction(mappedExtendedTransactionBuilder.build())
                .build();

        //when
        var result = transactionMapper.map(returnedTransaction);

        //then
        assertThat(result).isEqualTo(expectedMappedTransaction);
    }

}