package com.yolt.providers.stet.cicgroup.common.mapper.account;

import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.account.DefaultAccountMapper;
import nl.ing.lovebird.extendeddata.account.*;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class CicGroupAccountMapper extends DefaultAccountMapper {

    private final DateTimeSupplier dateTimeSupplier;

    public CicGroupAccountMapper(DateTimeSupplier dateTimeSupplier) {
        super(dateTimeSupplier);
        this.dateTimeSupplier = dateTimeSupplier;
    }

    @Override
    public ProviderAccountDTO mapToProviderAccountDTO(StetAccountDTO account, List<StetBalanceDTO> balances, List<ProviderTransactionDTO> providerTransactions) {
        return ProviderAccountDTO.builder()
                .accountId(account.getResourceId())
                .accountNumber(mapToProviderAccountNumberDTO(account, mapToHolderName(account)))
                .name(account.getName())
                .bic(account.getBicFi())
                .yoltAccountType(mapToAccountType(account.getType()))
                .currency(mapToCurrencyCode(account, balances))
                .lastRefreshed(dateTimeSupplier.getDefaultZonedDateTime())
                .linkedAccount(account.getLinkedAccount())
                .creditCardData(mapToProviderCreditCardDTO(account, getBalanceAmount(balances, getPreferredCardBalanceType())))
                .availableBalance(getBalanceAmount(balances, getPreferredAvailableBalanceTypes()))
                .currentBalance(getBalanceAmount(balances, getPreferredCurrentBalanceTypes()))
                .extendedAccount(mapToExtendedAccountDTO(account, balances))
                .transactions(providerTransactions)
                .accountMaskedIdentification(mapToMaskedIdentification(account))
                .build();
    }

    private String mapToMaskedIdentification(StetAccountDTO account) {
        if (account.getOther() == null || account.getOther().isEmpty() ||
            !StringUtils.hasText(account.getOther().get("identification"))) {
            return null;
        }
        return account.getOther().get("identification");
    }

    @Override
    protected List<AccountReferenceDTO> mapToAccountReferenceDTOs(StetAccountDTO account) {
        if (account.getIban() != null) {
            return super.mapToAccountReferenceDTOs(account);
        }
        return Optional.ofNullable(account.getOther())
                .map(other -> other.get("identification"))
                .map(maskedPan -> Collections.singletonList(AccountReferenceDTO.builder()
                        .type(AccountReferenceType.MASKED_PAN)
                        .value(maskedPan)
                        .build()))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<StetBalanceType> getPreferredCurrentBalanceTypes() {
        return List.of(StetBalanceType.CLBD, StetBalanceType.XPCD, StetBalanceType.OTHR);
    }

    @Override
    public ExtendedAccountDTO mapToExtendedAccountDTO(StetAccountDTO account,
                                                      List<StetBalanceDTO> balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .name(account.getName())
                .accountReferences(mapToAccountReferenceDTOs(account))
                .currency(mapToCurrencyCode(account, balances))
                .product(account.getProduct())
                .cashAccountType(mapToExternalCashAccountType(account.getType()))
                .bic(account.getBicFi())
                .linkedAccounts(account.getLinkedAccount())
                .usage(UsageType.PRIVATE)
                .details(account.getDetails())
                .balances(mapToBalanceDTOs(balances, account.getCurrency()))
                .status(Status.ENABLED)
                .build();
    }

    public List<BalanceDTO> mapToBalanceDTOs(List<StetBalanceDTO> balances,
                                             CurrencyCode currency) {
        List<BalanceDTO> balanceDTOs = new ArrayList<>();
        Map<StetBalanceType, StetBalanceDTO> filteredBalances = filterBalanceResources(balances, currency);

        StetBalanceDTO balanceClosingBooked = filteredBalances.get(StetBalanceType.CLBD);
        if (balanceClosingBooked != null) {
            balanceDTOs.add(mapToBalanceDTO(balanceClosingBooked, BalanceType.CLOSING_BOOKED));
        }
        StetBalanceDTO balanceExpected = filteredBalances.get(StetBalanceType.XPCD);
        if (balanceExpected != null) {
            balanceDTOs.add(mapToBalanceDTO(balanceExpected, BalanceType.EXPECTED));
        }
        return balanceDTOs;
    }

    @Override
    protected CurrencyCode mapToCurrencyCode(StetAccountDTO account, List<StetBalanceDTO> balances) {
        return Optional.ofNullable(account.getCurrency())
                .orElseGet(() -> {
                    List<CurrencyCode> currenciesFromBalances = balances.stream()
                            .map(StetBalanceDTO::getCurrency)
                            .distinct()
                            .collect(Collectors.toList());

                    if (currenciesFromBalances.size() == 1) {
                        return currenciesFromBalances.get(0);
                    } else {
                        return null;
                    }
                });
    }

    @Override
    protected BigDecimal getBalanceAmount(List<StetBalanceDTO> balances, List<StetBalanceType> preferredBalanceTypes) {
        Map<StetBalanceType, BigDecimal> balanceAmountMap = new HashMap<>();
        OffsetDateTime othrBalanceDate = null;
        for (StetBalanceDTO balance : balances) {
            if (balance.getType().equals(StetBalanceType.OTHR)) {
                if (othrBalanceDate == null || othrBalanceDate.isBefore(balance.getReferenceDate())) {
                    othrBalanceDate = balance.getReferenceDate();
                    balanceAmountMap.put(balance.getType(), balance.getAmount());
                }
            } else {
                balanceAmountMap.put(balance.getType(), balance.getAmount());
            }
        }

        for (StetBalanceType preferredBalanceType : preferredBalanceTypes) {
            if (balanceAmountMap.containsKey(preferredBalanceType)) {
                return balanceAmountMap.get(preferredBalanceType);
            }
        }
        return null;
    }

    private BalanceDTO mapToBalanceDTO(final StetBalanceDTO balance,
                                       final BalanceType balanceType) {
        return BalanceDTO.builder()
                .balanceType(balanceType)
                .balanceAmount(BalanceAmountDTO.builder()
                        .amount(balance.getAmount())
                        .currency(balance.getCurrency())
                        .build())
                .referenceDate(dateTimeSupplier.convertToZonedDateTime(balance.getReferenceDate()))
                .build();
    }

    private Map<StetBalanceType, StetBalanceDTO> filterBalanceResources(final List<StetBalanceDTO> balanceResources,
                                                                        final CurrencyCode currency) {
        EnumMap<StetBalanceType, StetBalanceDTO> balances = new EnumMap<>(StetBalanceType.class);

        StetBalanceDTO balanceExpected = extractBalanceForAccount(balanceResources, StetBalanceType.XPCD, currency).orElse(null);
        balances.put(StetBalanceType.XPCD, balanceExpected);
        balances.put(StetBalanceType.CLBD, extractBalanceForAccount(balanceResources, StetBalanceType.CLBD, currency).orElse(balanceExpected));
        balances.put(StetBalanceType.OTHR, extractBalanceForAccount(balanceResources, StetBalanceType.OTHR, currency).orElse(balanceExpected));

        return balances;
    }

    private Optional<StetBalanceDTO> extractBalanceForAccount(final List<StetBalanceDTO> balanceResources,
                                                              final StetBalanceType balanceStatus,
                                                              final CurrencyCode currency) {
        for (StetBalanceDTO balanceResource : balanceResources) {
            if (balanceResource.getType().equals(balanceStatus)) {
                if (balanceResource.getCurrency().equals(currency)) {
                    return Optional.of(balanceResource);
                }
            }
        }
        return Optional.empty();
    }
}
