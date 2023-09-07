package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper;

import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Transaction;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providerdomain.YoltCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DefaultRaiffeisenAtGroupTransactionMapperTest {

    private ZoneId zoneId = ZoneId.of("Europe/Vienna");
    private Clock clock = Clock.fixed(Instant.parse("2022-01-01T00:00:00Z"), ZoneId.of("UTC"));

    private DefaultRaiffeisenAtGroupTransactionMapper transactionMapper;

    @BeforeEach
    void setUp() {
        transactionMapper = new DefaultRaiffeisenAtGroupTransactionMapper(
                new DefaultRaiffeisenAtGroupDateMapper(zoneId, clock)
        );
    }

    @Test
    void shouldMappedCreditTransaction() {
        //given
        var transactionStatus = TransactionStatus.BOOKED;
        var transactionFromBank = mock(Transaction.class);
        given(transactionFromBank.getTransactionId()).willReturn("22123460");
        given(transactionFromBank.getBookingDate()).willReturn(LocalDate.of(2022, 07, 05));
        given(transactionFromBank.getValueDate()).willReturn(LocalDate.of(2022, 07, 06));
        given(transactionFromBank.getCurrency()).willReturn("EUR");
        given(transactionFromBank.getAmount()).willReturn(new BigDecimal("9820.99"));
        given(transactionFromBank.getCreditorName()).willReturn("Creditor Name");
        given(transactionFromBank.getCreditorIban()).willReturn("AT439900000000010017");
        given(transactionFromBank.getDebtorName()).willReturn("Debtor Name");
        given(transactionFromBank.getDebtorIban()).willReturn("AT099900000000001511");
        given(transactionFromBank.getRemittanceInformationUnstructured()).willReturn("Incoming payment");
        var expectedMappedTransaction = ProviderTransactionDTO.builder()
                .externalId("22123460")
                .dateTime(LocalDate.of(2022, 07, 05).atStartOfDay(zoneId))
                .type(ProviderTransactionType.CREDIT)
                .category(YoltCategory.GENERAL)
                .amount(new BigDecimal("9820.99"))
                .description("Incoming payment")
                .status(transactionStatus)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .bookingDate(LocalDate.of(2022, 07, 05).atStartOfDay(zoneId))
                        .valueDate(LocalDate.of(2022, 07, 06).atStartOfDay(zoneId))
                        .status(TransactionStatus.BOOKED)
                        .remittanceInformationUnstructured("Incoming payment")
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(new BigDecimal("9820.99"))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .creditorName("Creditor Name")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT439900000000010017")
                                .build())
                        .debtorName("Debtor Name")
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT099900000000001511")
                                .build())
                        .transactionIdGenerated(true)
                        .build())
                .build();

        //when
        var result = transactionMapper.map(transactionFromBank, transactionStatus);

        //then
        assertThat(result).isEqualTo(expectedMappedTransaction);
    }

    @Test
    void shouldReturnMappedAccountWithoutCurrency() {
        //given
        var transactionFromBank = mock(Transaction.class);
        given(transactionFromBank.getAmount()).willReturn(new BigDecimal("1.00"));
        given(transactionFromBank.getCurrency()).willReturn("");

        //when
        var result = transactionMapper.map(transactionFromBank, TransactionStatus.BOOKED);

        //then
        assertThat(result.getExtendedTransaction().getTransactionAmount().getCurrency()).isNull();
    }

    @Test
    void shouldReturnDebitAccountWithoutDebtor() {
        //given
        var transactionFromBank = mock(Transaction.class);
        given(transactionFromBank.getAmount()).willReturn(new BigDecimal("-1.00"));
        var expectedCreditorName = "Creditor Name";
        var expectedIban = "AT12345";
        given(transactionFromBank.getCreditorName()).willReturn(expectedCreditorName);
        given(transactionFromBank.getCreditorIban()).willReturn(expectedIban);
        var expectedAccountReference = AccountReferenceDTO.builder()
                .type(AccountReferenceType.IBAN)
                .value(expectedIban)
                .build();

        //when
        var result = transactionMapper.map(transactionFromBank, TransactionStatus.PENDING);

        //then
        assertThat(result.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(result.getExtendedTransaction().getCreditorName()).isEqualTo(expectedCreditorName);
        assertThat(result.getExtendedTransaction().getCreditorAccount()).isEqualTo(expectedAccountReference);
    }
}