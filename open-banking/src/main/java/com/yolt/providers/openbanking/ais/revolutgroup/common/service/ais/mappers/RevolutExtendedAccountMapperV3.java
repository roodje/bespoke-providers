package com.yolt.providers.openbanking.ais.revolutgroup.common.service.ais.mappers;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.AccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.DefaultExtendedAccountMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount4Account;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataBalance;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class RevolutExtendedAccountMapperV3 extends DefaultExtendedAccountMapper {

    public RevolutExtendedAccountMapperV3(AccountReferenceTypeMapper accountReferenceTypeMapper, Function<String, CurrencyCode> currencyCodeMapper, Function<List<OBReadBalance1DataBalance>, List<BalanceDTO>> balancesMapper) {
        super(accountReferenceTypeMapper, currencyCodeMapper, balancesMapper);
    }

    @Override
    public ExtendedAccountDTO.ExtendedAccountDTOBuilder getBuilder(OBAccount6 account, String extractedPrimaryAccountName, List<OBReadBalance1DataBalance> balances) {
        account.setAccount(getNotNullAccountReferences(account.getAccount()));
        return super.getBuilder(account, extractedPrimaryAccountName, balances);
    }

    /**
     * Message from the Revolut relating to null value of account field in response:
     * </p>
     * This could happen with different currency accounts. In EUR pocket you can have 2 addresses, one for SWIFT and one for SEPA.
     * In GBP, you can have local details (account number and sort code) and IBAN for SWIFT transfers.
     * Some information may be missing from specific pocket if the account is not eligible to receive local GBP details or if the currency pocket was just created.
     * It may take few hours (and for local GBP around 24 hours) for details to be generated.
     */
    private List<OBAccount4Account> getNotNullAccountReferences(final List<OBAccount4Account> allAccountReferences) {
        return Objects.requireNonNullElse(allAccountReferences, Collections.emptyList());
    }
}
