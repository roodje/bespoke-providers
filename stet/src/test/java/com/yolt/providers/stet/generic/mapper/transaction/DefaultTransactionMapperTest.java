package com.yolt.providers.stet.generic.mapper.transaction;

import com.yolt.providers.stet.generic.dto.TestStetTransactionDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionIndicator;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionStatus;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultTransactionMapperTest {

    private DefaultTransactionMapper transactionMapper;

    @BeforeEach
    void initialize() {
        transactionMapper = new DefaultTransactionMapper(new DateTimeSupplier(Clock.systemUTC()));
    }

    @Test
    void shouldMapProviderTransactionDTOs() {
        // given
        List<StetTransactionDTO> transactions = List.of(
                createTransactionResourceDTO(StetTransactionStatus.BOOK, StetTransactionIndicator.DBIT),
                createTransactionResourceDTO(StetTransactionStatus.PDNG, StetTransactionIndicator.CRDT));

        // when
        List<ProviderTransactionDTO> transactionDTOs = transactionMapper.mapToProviderTransactionDTOs(transactions);

        // then
        ProviderTransactionDTO bookedTransactionDTO = transactionDTOs.get(0);
        assertThat(bookedTransactionDTO).satisfies(validateTransactionDTO(ProviderTransactionType.DEBIT, TransactionStatus.BOOKED));

        ProviderTransactionDTO pendingTransactionDTO = transactionDTOs.get(1);
        assertThat(pendingTransactionDTO).satisfies(validateTransactionDTO(ProviderTransactionType.CREDIT, TransactionStatus.PENDING));
    }

    @ParameterizedTest
    @CsvSource({"BOOK,DBIT,BOOKED,DEBIT", "PDNG,CRDT,PENDING,CREDIT"})
    void shouldMapToProviderTransactionDTO(String inputTransactionType,
                                           String inputTransactionIndicator,
                                           String expectedTransactionStatus,
                                           String expectedTransactionType) {
        // given
        StetTransactionStatus type = StetTransactionStatus.valueOf(inputTransactionType);
        StetTransactionIndicator indicator = StetTransactionIndicator.valueOf(inputTransactionIndicator);
        StetTransactionDTO transaction = createTransactionResourceDTO(type, indicator);

        // when
        ProviderTransactionDTO transactionDTO = transactionMapper.mapToProviderTransactionDTO(transaction);

        // then
        assertThat(transactionDTO).satisfies(validateTransactionDTO(
                ProviderTransactionType.valueOf(expectedTransactionType),
                nl.ing.lovebird.extendeddata.transaction.TransactionStatus.valueOf(expectedTransactionStatus)));
    }

    @ParameterizedTest
    @CsvSource({"BOOK,DBIT,BOOKED,DEBIT", "PDNG,CRDT,PENDING,CREDIT"})
    void shouldMapToExtendedProviderTransactionDTO(String inputTransactionStatus,
                                                   String inputCreditDebitIndicator,
                                                   String expectedTransactionStatus,
                                                   String expectedTransactionType) {
        // given
        StetTransactionStatus status = StetTransactionStatus.valueOf(inputTransactionStatus);
        StetTransactionIndicator indicator = StetTransactionIndicator.valueOf(inputCreditDebitIndicator);
        StetTransactionDTO transaction = createTransactionResourceDTO(status, indicator);

        // when
        ExtendedTransactionDTO extendedTransactionDTO = transactionMapper.mapToExtendedTransactionDTO(transaction);

        // then
        assertThat(extendedTransactionDTO).satisfies(validateExtendedTransactionDTO(
                ProviderTransactionType.valueOf(expectedTransactionType),
                nl.ing.lovebird.extendeddata.transaction.TransactionStatus.valueOf(expectedTransactionStatus)));
    }

    @ParameterizedTest
    @CsvSource({"BOOK,BOOKED", "PDNG,PENDING"})
    void shouldMapToTransactionStatus(String inputTransactionStatus, String expectedTransactionStatus) {
        // given
        StetTransactionStatus type = StetTransactionStatus.valueOf(inputTransactionStatus);

        // when
        TransactionStatus transactionStatus = transactionMapper.mapToTransactionStatus(type);

        // then
        assertThat(transactionStatus).isEqualTo(TransactionStatus.valueOf(expectedTransactionStatus));
    }

    @ParameterizedTest
    @CsvSource({"DBIT,DEBIT", "CRDT,CREDIT"})
    void shouldMapToProviderTransactionType(String inputTransactionIndicator, String expectedProviderTransactionType) {
        // given
        StetTransactionIndicator indicator = StetTransactionIndicator.valueOf(inputTransactionIndicator);

        // when
        ProviderTransactionType providerTransactionType = transactionMapper.mapToTransactionType(indicator);

        // then
        assertThat(providerTransactionType).isEqualTo(ProviderTransactionType.valueOf(expectedProviderTransactionType));
    }

    @Test
    void shouldMapToAccountReferenceDTO() {
        // given
        String iban = "FR7317569000507664932713I67";

        // when
        AccountReferenceDTO accountReferenceDTO = transactionMapper.mapToAccountReferenceDTOs(iban);

        // then
        assertThat(accountReferenceDTO.getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(accountReferenceDTO.getValue()).isEqualTo(iban);
    }

    @ParameterizedTest
    @CsvSource({"DBIT,-20.50", "CRDT,20.50"})
    void shouldMapToBalanceAmountDTO(String inputTransactionIndicator, String expectedAmount) {
        // given
        StetTransactionIndicator indicator = StetTransactionIndicator.valueOf(inputTransactionIndicator);
        StetTransactionDTO transaction = createTransactionResourceDTO(StetTransactionStatus.BOOK, indicator);

        // when
        BalanceAmountDTO balanceAmountDTO = transactionMapper.mapToBalanceAmountDTO(transaction);

        // then
        assertThat(balanceAmountDTO.getAmount()).isEqualTo(expectedAmount);
        assertThat(balanceAmountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);
    }

    @ParameterizedTest
    @CsvSource({"DBIT,10.10,-10.10", "CRDT,20.20,20.20"})
    void shouldAdjustAmountSignIndicatorBasedOnGivenCreditDebitIndicator(String inputTransactionIndicator, String amountToAdjust, String expectedAmount) {
        // given
        StetTransactionIndicator indicator = StetTransactionIndicator.valueOf(inputTransactionIndicator);
        BigDecimal amount = new BigDecimal(amountToAdjust);

        // when
        BigDecimal adjustedAmount = transactionMapper.adjustSignIndicator(amount, indicator);

        // then
        assertThat(adjustedAmount).isEqualTo(expectedAmount);
    }

    private StetTransactionDTO createTransactionResourceDTO(StetTransactionStatus type,
                                                            StetTransactionIndicator indicator) {
        return TestStetTransactionDTO.builder()
                .bookingDate(OffsetDateTime.now())
                .amount(new BigDecimal("20.50"))
                .currency(CurrencyCode.EUR)
                .transactionIndicator(indicator)
                .creditorIban("FR4230003000306327748225F14")
                .creditorName("CreditorName")
                .creditorIdentification("CreditorIdentification")
                .ultimateCreditorName("UltimateCreditorName")
                .debtorIban("FR9932003000306327748225F14")
                .debtorName("DebtorName")
                .ultimateDebtorName("UltimateDebtorName")
                .valueDate(OffsetDateTime.now())
                .endToEndId("EndToEndId")
                .resourceId("ResourceId")
                .entryReference("EntryReference")
                .unstructuredRemittanceInformation(List.of("RemittanceInformation1", "RemittanceInformation2"))
                .bankTransactionCode("BankTransactionCode")
                .status(type)
                .build();
    }

    private Consumer<ProviderTransactionDTO> validateTransactionDTO(ProviderTransactionType type,
                                                                    TransactionStatus status) {
        return (transactionDTO) -> {
            // validate ProviderTransactionDTO
            assertThat(transactionDTO.getExternalId()).isEqualTo("EntryReference");
            assertThat(transactionDTO.getCategory()).isEqualTo(YoltCategory.GENERAL);
            assertThat(transactionDTO.getDescription()).isEqualTo("RemittanceInformation1, RemittanceInformation2");
            assertThat(transactionDTO.getDateTime()).isNotNull();
            assertThat(transactionDTO.getType()).isEqualTo(type);
            assertThat(transactionDTO.getStatus()).isEqualTo(status);
            assertThat(transactionDTO.getAmount()).isEqualTo("20.50");
            assertThat(transactionDTO.getMerchant()).isNull();

            Consumer<ExtendedTransactionDTO> extendedTransactionDTOConsumer = validateExtendedTransactionDTO(type, status);
            extendedTransactionDTOConsumer.accept(transactionDTO.getExtendedTransaction());
        };
    }

    private Consumer<ExtendedTransactionDTO> validateExtendedTransactionDTO(ProviderTransactionType type,
                                                                            TransactionStatus status) {
        return (extendedTransactionDTO) -> {
            // validate ExtendedTransactionDTO
            assertThat(extendedTransactionDTO.getRemittanceInformationUnstructured()).isEqualTo("RemittanceInformation1, RemittanceInformation2");
            assertThat(extendedTransactionDTO.getValueDate()).isNotNull();
            assertThat(extendedTransactionDTO.getBookingDate()).isNotNull();
            assertThat(extendedTransactionDTO.getStatus()).isEqualTo(status);
            assertThat(extendedTransactionDTO.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
            assertThat(extendedTransactionDTO.getEndToEndId()).isEqualTo("EndToEndId");
            if (ProviderTransactionType.DEBIT.equals(type)) {
                assertThat(extendedTransactionDTO.getTransactionAmount().getAmount()).isEqualTo("-20.50");
            } else if (ProviderTransactionType.CREDIT.equals(type)) {
                assertThat(extendedTransactionDTO.getTransactionAmount().getAmount()).isEqualTo("20.50");
            }
        };
    }
}
