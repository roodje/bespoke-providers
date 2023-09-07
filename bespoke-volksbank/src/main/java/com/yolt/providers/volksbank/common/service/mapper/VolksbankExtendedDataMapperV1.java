package com.yolt.providers.volksbank.common.service.mapper;

import com.yolt.providers.volksbank.dto.v1_1.AccountDetails;
import com.yolt.providers.volksbank.dto.v1_1.BalanceItem;
import com.yolt.providers.volksbank.dto.v1_1.TransactionItem;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class VolksbankExtendedDataMapperV1 implements VolksbankExtendedDataMapper {

    private static final ZoneId AMSTERDAM_ZONE_ID = ZoneId.of("Europe/Amsterdam");
    private final CurrencyCodeMapper currencyCodeMapper;

    @Override
    public ExtendedAccountDTO createExtendedAccountDTO(final AccountDetails account,
                                                       final BalanceItem balance) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .bic(account.getCustomerBic())
                .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban())))
                .balances(mapToBalances(balance))
                .currency(currencyCodeMapper.toCurrencyCode(account.getCurrency()))
                .name(account.getName())
                .product(account.getProduct())
                .build();
    }

    @Override
    public ExtendedTransactionDTO createExtendedTransactionDTO(final TransactionItem transaction, BigDecimal amount) {
        return ExtendedTransactionDTO.builder()
                .endToEndId(transaction.getEndToEndId())
                .mandateId(transaction.getMandateId())
                .purposeCode(transaction.getPurposeCode())
                .proprietaryBankTransactionCode(transaction.getProprietaryBankTransactionCode())
                .entryReference(transaction.getEntryReference())
                .bankTransactionCode(transaction.getBankTransactionCode().toString())
                .bookingDate(transaction.getBookingDate().atStartOfDay(AMSTERDAM_ZONE_ID))
                .valueDate(transaction.getValueDate().atStartOfDay(AMSTERDAM_ZONE_ID))
                .status(TransactionStatus.BOOKED)
                .transactionAmount(BalanceAmountDTO.builder()
                        .currency(CurrencyCode.valueOf(transaction.getTransactionAmount().getCurrency().name()))
                        .amount(amount)
                        .build())
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .remittanceInformationStructured(retrieveRemittanceInformationStructured(transaction))
                .creditorId(transaction.getCreditorId())
                .creditorName(transaction.getCreditorName())
                .creditorAccount(ObjectUtils.isEmpty(transaction.getCreditorAccount()) ? null : new AccountReferenceDTO(AccountReferenceType.IBAN, transaction.getCreditorAccount().getIban()))
                .ultimateCreditor(transaction.getUltimateCreditor())
                .debtorName(transaction.getDebtorName())
                .debtorAccount(ObjectUtils.isEmpty(transaction.getDebtorAccount()) ? null : new AccountReferenceDTO(AccountReferenceType.IBAN, transaction.getDebtorAccount().getIban()))
                .transactionIdGenerated(true)
                .build();
    }

    private static List<BalanceDTO> mapToBalances(final BalanceItem balance) {
        return Collections.singletonList(BalanceDTO.builder()
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.valueOf(balance.getBalanceAmount().getCurrency().name()), new BigDecimal(balance.getBalanceAmount().getAmount())))
                .balanceType(BalanceType.fromName(balance.getBalanceType().toString()))
                .lastChangeDateTime(balance.getLastChangeDateTime().toZonedDateTime())
                .build());
    }

    private String retrieveRemittanceInformationStructured(final TransactionItem transaction) {
        return Optional.ofNullable(transaction.getRemittanceInformationStructured())
                .map(rms -> String.format("%s %s", rms.getReferenceIssuer(), rms.getReference()))
                .orElse(null);
    }
}
