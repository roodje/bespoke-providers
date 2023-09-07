package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.AccountReferenceTypeMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount4Account;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataBalance;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.Status;
import nl.ing.lovebird.extendeddata.account.UsageType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
public class DefaultExtendedAccountMapper implements ExtendedAccountMapper {

    private final AccountReferenceTypeMapper accountReferenceTypeMapper;
    private final Function<String, CurrencyCode> currencyCodeMapper;
    private final Function<List<OBReadBalance1DataBalance>, List<BalanceDTO>> balancesMapper;

    public ExtendedAccountDTO.ExtendedAccountDTOBuilder getBuilder(final OBAccount6 account,
                                                                   final String extractedPrimaryAccountName,
                                                                   final List<OBReadBalance1DataBalance> balances) {
        List<OBAccount4Account> data2Account = account.getAccount();
        return ExtendedAccountDTO.builder()
                .resourceId(account.getAccountId())
                .status(Status.ENABLED)
                .usage(UsageTypeMapper.map(account.getAccountType()))
                .bic(account.getServicer() == null ? null : account.getServicer().getIdentification())
                .accountReferences(mapToAccountReferences(data2Account))
                .balances(balancesMapper.apply(balances))
                .currency(currencyCodeMapper.apply(account.getCurrency()))
                .name(extractedPrimaryAccountName);
    }

    public final ExtendedAccountDTO mapToExtendedModelAccount(final OBAccount6 account,
                                                              final String extractedPrimaryAccountName,
                                                              final List<OBReadBalance1DataBalance> balances) {
        return getBuilder(account, extractedPrimaryAccountName, balances)
                .build();
    }

    private List<AccountReferenceDTO> mapToAccountReferences(final List<OBAccount4Account> accounts) {
        List<AccountReferenceDTO> list = new ArrayList<>();
        if (accounts != null) {
            for (OBAccount4Account account : accounts) {
                accountReferenceTypeMapper.map(account.getSchemeName(), account.getIdentification()).ifPresent(
                        accountReferenceType -> list.add(new AccountReferenceDTO(accountReferenceType, account.getIdentification()))
                );
            }
        }
        return list;
    }

    private static class UsageTypeMapper {

        private static final Map<OBExternalAccountType1Code, UsageType> USAGE_MAPPER = new EnumMap<>(OBExternalAccountType1Code.class);

        static {
            USAGE_MAPPER.put(OBExternalAccountType1Code.BUSINESS, UsageType.CORPORATE);
            USAGE_MAPPER.put(OBExternalAccountType1Code.PERSONAL, UsageType.PRIVATE);
        }

        private static UsageType map(final OBExternalAccountType1Code typeEnum) {
            return USAGE_MAPPER.get(typeEnum);
        }
    }
}
