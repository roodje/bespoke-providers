package com.yolt.providers.redsys.common.service.mapper;

import com.yolt.providers.redsys.common.dto.AccountDetails;
import com.yolt.providers.redsys.common.dto.Amount;
import com.yolt.providers.redsys.common.dto.Balance;
import com.yolt.providers.redsys.common.dto.Transaction;
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
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AllArgsConstructor
public class RedsysExtendedDataMapperV2 {

    private final CurrencyCodeMapper currencyCodeMapper;
    private final ZoneId zoneId;
    private static final Pattern REMITTANCE_INFO_UNSTRUCTURED_PATTERN = Pattern.compile("(.*/TXT/(H\\||D\\|)?)?(.*)");

    public ExtendedAccountDTO createExtendedAccountDTO(final AccountDetails account,
                                                       final List<Balance> balances,
                                                       final String accountName) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .bic(account.getBic())
                .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban())))
                .balances(toBalances(balances))
                .currency(currencyCodeMapper.toCurrencyCode(account.getCurrency()))
                .name(accountName)
                .product(account.getProduct())
                .build();
    }

    public ExtendedTransactionDTO toExtendedTransactionDTO(final Transaction transaction,
                                                           final TransactionStatus transactionStatus) {
        return ExtendedTransactionDTO.builder()
                .endToEndId(transaction.getEndToEndId())
                .mandateId(transaction.getMandateId())
                .entryReference(transaction.getEntryReference())
                .bankTransactionCode(transaction.getBankTransactionCode())
                .bookingDate(parseLocalDateWithAddedZonedTime(transaction.getBookingDate()))
                .valueDate(parseLocalDateWithAddedZonedTime(transaction.getValueDate()))
                .status(transactionStatus)
                .transactionAmount(toBalanceAmount(transaction))
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .remittanceInformationStructured(transaction.getRemittanceInformationStructured())
                .creditorId(transaction.getCreditorId())
                .creditorName(transaction.getCreditorName())
                .creditorAccount(transaction.getCreditorAccount() != null ? new AccountReferenceDTO(AccountReferenceType.IBAN, transaction.getCreditorAccount().getIban()) : null)
                .ultimateCreditor(transaction.getUltimateCreditor())
                .purposeCode(transaction.getPurposeCode())
                .debtorName(transaction.getDebtorName())
                .debtorAccount(transaction.getDebtorAccount() != null ? new AccountReferenceDTO(AccountReferenceType.IBAN, transaction.getDebtorAccount().getIban()) : null)
                .transactionIdGenerated(true)
                .build();
    }

    public ZonedDateTime parseLocalDateWithAddedZonedTime(final String date) {
        return StringUtils.isEmpty(date) ? null : ZonedDateTime.from(LocalDate.parse(date).atStartOfDay(zoneId));
    }

    protected String formatRemittanceInformation(String remittanceInformationUnstructured) {
        Matcher matcher = REMITTANCE_INFO_UNSTRUCTURED_PATTERN.matcher(remittanceInformationUnstructured);
        return matcher.find() ? matcher.group(3) : remittanceInformationUnstructured;
    }

    private List<BalanceDTO> toBalances(final List<Balance> balances) {
        return balances.stream()
                .map(rawBalances -> BalanceDTO.builder()
                        .balanceAmount(toBalanceAmount(rawBalances.getBalanceAmount()))
                        .balanceType(BalanceType.fromName(rawBalances.getBalanceType()))
                        .lastChangeDateTime(toLastChangeDateTime(rawBalances.getLastChangeDateTime()))
                        .referenceDate(toReferenceDate(rawBalances.getReferenceDate()))
                        .build())
                .collect(Collectors.toList());
    }

    private ZonedDateTime toReferenceDate(final String referenceDate) {
        return StringUtils.isEmpty(referenceDate) ? null : ZonedDateTime.from(LocalDate.parse(referenceDate).atStartOfDay(zoneId));
    }

    protected ZonedDateTime toLastChangeDateTime(final String lastChangeDateTime) {
        return StringUtils.isEmpty(lastChangeDateTime) ? null : ZonedDateTime.parse(lastChangeDateTime);
    }

    private BalanceAmountDTO toBalanceAmount(final Amount balanceAmount) {
        String amountInBigDecimalFormat = balanceAmount.getAmount().replace(",", ".");
        return new BalanceAmountDTO(
                CurrencyCode.valueOf(balanceAmount.getCurrency()),
                new BigDecimal(amountInBigDecimalFormat).abs());
    }

    protected BalanceAmountDTO toBalanceAmount(final Transaction transaction) {
        return BalanceAmountDTO.builder()
                .currency(CurrencyCode.valueOf(transaction.getTransactionAmount().getCurrency()))
                .amount(new BigDecimal(transaction.getTransactionAmount().getAmount()))
                .build();
    }
}
