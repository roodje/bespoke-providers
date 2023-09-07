package com.yolt.providers.stet.bnpparibasgroup.mapper;

import com.yolt.providers.stet.bnpparibasfortisgroup.common.mapper.transaction.BnpParibasFortisGroupTransactionMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.BnpParibasGroupDataTimeSupplier;
import com.yolt.providers.stet.generic.dto.TestStetTransactionDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionIndicator;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionStatus;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static com.yolt.providers.stet.generic.dto.transaction.StetTransactionIndicator.CRDT;
import static com.yolt.providers.stet.generic.dto.transaction.StetTransactionIndicator.DBIT;
import static com.yolt.providers.stet.generic.dto.transaction.StetTransactionStatus.BOOK;
import static com.yolt.providers.stet.generic.dto.transaction.StetTransactionStatus.PDNG;
import static org.assertj.core.api.Assertions.assertThat;

public class BnpParibasGroupTransactionMapperTest {

    private static final String SUPPORTED_CURRENCY = "EUR";
    private static final String ENTRY_REFERENCE = "AF5T2";
    private static final String REMITTANCE_INFORMATION = "SEPA CREDIT TRANSFER from PSD2Company";
    private static final ZonedDateTime ZONED_DATE_TIME = ZonedDateTime.parse("2018-02-12T23:00Z");

    private static final BigDecimal POSITIVE_AMOUNT = new BigDecimal("100.10");
    private static final BigDecimal NEGATIVE_AMOUNT = new BigDecimal("-100.10");
    private static final LocalDate LOCAL_DATE = LocalDate.of(2018, 2, 13);
    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.of(LOCAL_DATE, LocalTime.of(8, 0), ZoneId.of("Europe/Paris").getRules().getOffset(LocalDateTime.now()));
    private static final CurrencyCode CURRENCY_CODE = CurrencyCode.valueOf(SUPPORTED_CURRENCY);

    private BnpParibasFortisGroupTransactionMapper transactionMapper;

    @BeforeEach
    public void setup() {
        DateTimeSupplier dateTimeSupplier = new BnpParibasGroupDataTimeSupplier(Clock.systemUTC(), ZoneId.of("Europe/Paris"));
        transactionMapper = new BnpParibasFortisGroupTransactionMapper(dateTimeSupplier);
    }

    @Test
    public void shouldMapToProviderTransactionDTOForCredit() {
        // given
        StetTransactionDTO creditTransaction = createBookedTransaction(POSITIVE_AMOUNT, CRDT, OFFSET_DATE_TIME);

        // when
        ProviderTransactionDTO transactionDTO = mapToProviderTransactionDtoByMapper(creditTransaction);

        // then
        assertThat(transactionDTO.getExternalId()).isEqualTo(ENTRY_REFERENCE);
        assertThat(transactionDTO.getDateTime()).isEqualTo(ZONED_DATE_TIME);
        assertThat(transactionDTO.getAmount()).isEqualTo(POSITIVE_AMOUNT);
        assertThat(transactionDTO.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transactionDTO.getType()).isEqualTo(ProviderTransactionType.CREDIT);

        ExtendedTransactionDTO extendedTransactionDTO = transactionDTO.getExtendedTransaction();
        assertThat(extendedTransactionDTO.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(extendedTransactionDTO.getEntryReference()).isEqualTo(ENTRY_REFERENCE);
        assertThat(extendedTransactionDTO.getBookingDate()).isEqualTo(ZONED_DATE_TIME);
        assertThat(extendedTransactionDTO.getValueDate()).isEqualTo(ZONED_DATE_TIME);
        assertThat(extendedTransactionDTO.getRemittanceInformationUnstructured()).isEqualTo(REMITTANCE_INFORMATION);

        BalanceAmountDTO transactionAmount = extendedTransactionDTO.getTransactionAmount();
        assertThat(transactionAmount.getAmount()).isEqualTo(POSITIVE_AMOUNT);
        assertThat(transactionAmount.getCurrency()).isEqualTo(CURRENCY_CODE);

        transactionDTO.validate();
    }

    @Test
    public void shouldMapToProviderTransactionDTOForDebit() {
        // given
        StetTransactionDTO debitTransaction = createPendingTransaction(POSITIVE_AMOUNT, DBIT, OFFSET_DATE_TIME);

        // when
        ProviderTransactionDTO transactionDTO = mapToProviderTransactionDtoByMapper(debitTransaction);

        // then
        assertThat(transactionDTO.getExternalId()).isEqualTo(ENTRY_REFERENCE);
        assertThat(transactionDTO.getDateTime()).isEqualTo(ZONED_DATE_TIME);
        assertThat(transactionDTO.getAmount()).isEqualTo(POSITIVE_AMOUNT);
        assertThat(transactionDTO.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transactionDTO.getType()).isEqualTo(ProviderTransactionType.DEBIT);

        ExtendedTransactionDTO extendedTransactionDTO = transactionDTO.getExtendedTransaction();
        assertThat(extendedTransactionDTO.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(extendedTransactionDTO.getEntryReference()).isEqualTo(ENTRY_REFERENCE);
        assertThat(extendedTransactionDTO.getBookingDate()).isEqualTo(ZONED_DATE_TIME);
        assertThat(extendedTransactionDTO.getValueDate()).isEqualTo(ZONED_DATE_TIME);
        assertThat(extendedTransactionDTO.getRemittanceInformationUnstructured()).isEqualTo(REMITTANCE_INFORMATION);

        BalanceAmountDTO transactionAmount = extendedTransactionDTO.getTransactionAmount();
        assertThat(transactionAmount.getAmount()).isEqualTo(NEGATIVE_AMOUNT);
        assertThat(transactionAmount.getCurrency()).isEqualTo(CurrencyCode.EUR);

    }

    @Test
    public void shouldMapToProviderTransactionDTOForCreditWithNegativeAmount() {
        // given
        StetTransactionDTO creditTransaction = createBookedTransaction(NEGATIVE_AMOUNT, CRDT, OFFSET_DATE_TIME);

        // when
        ProviderTransactionDTO transactionDTO = mapToProviderTransactionDtoByMapper(creditTransaction);

        // then
        assertThat(transactionDTO.getAmount()).isEqualTo(POSITIVE_AMOUNT);
    }

    @Test
    public void shouldMapToProviderTransactionDTOForDebitWithNegativeAmount() {
        // given
        StetTransactionDTO debitTransaction = createBookedTransaction(NEGATIVE_AMOUNT, DBIT, OFFSET_DATE_TIME);

        // when
        ProviderTransactionDTO transactionDTO = mapToProviderTransactionDtoByMapper(debitTransaction);

        // then
        assertThat(transactionDTO.getAmount()).isEqualTo(POSITIVE_AMOUNT);
    }

    @Test
    public void shouldMapNullToDataTimeWhenNullIsInDTO() {
        // given
        StetTransactionDTO transaction = createBookedTransaction(NEGATIVE_AMOUNT, CRDT, null);

        // when
        ProviderTransactionDTO transactionDTO = mapToProviderTransactionDtoByMapper(transaction);

        //then
        assertThat(transactionDTO.getDateTime()).isNull();
    }

    private ProviderTransactionDTO mapToProviderTransactionDtoByMapper(StetTransactionDTO creditTransaction) {
        return transactionMapper.mapToProviderTransactionDTOs(List.of(creditTransaction)).get(0);
    }

    private StetTransactionDTO createBookedTransaction(BigDecimal amount,
                                                       StetTransactionIndicator transactionIndicator,
                                                       OffsetDateTime dateTime) {
        return createTransaction(amount, transactionIndicator, dateTime, BOOK);
    }

    private StetTransactionDTO createPendingTransaction(BigDecimal amount,
                                                        StetTransactionIndicator transactionIndicator,
                                                        OffsetDateTime dateTime) {
        return createTransaction(amount, transactionIndicator, dateTime, PDNG);
    }

    private StetTransactionDTO createTransaction(BigDecimal amount,
                                                 StetTransactionIndicator transactionIndicator,
                                                 OffsetDateTime dateTime, StetTransactionStatus status) {

        return TestStetTransactionDTO.builder()
                .resourceId("12")
                .entryReference(ENTRY_REFERENCE)
                .amount(amount)
                .transactionIndicator(transactionIndicator)
                .status(status)
                .bookingDate(dateTime)
                .amount(amount)
                .valueDate(dateTime)
                .currency(CURRENCY_CODE)
                .transactionDate(dateTime)
                .unstructuredRemittanceInformation(List.of(REMITTANCE_INFORMATION))
                .build();
    }

}