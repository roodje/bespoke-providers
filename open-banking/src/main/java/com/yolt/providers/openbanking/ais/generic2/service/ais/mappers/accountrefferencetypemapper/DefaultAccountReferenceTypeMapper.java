package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper;

import lombok.NoArgsConstructor;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Optional;

@NoArgsConstructor()
public class DefaultAccountReferenceTypeMapper implements AccountReferenceTypeMapper {

    private static final String IBAN = "UK.OBIE.IBAN";
    private static final String BBAN = "UK.OBIE.BBAN";
    private static final String PAN = "UK.OBIE.PAN";
    private static final String SORTCODEACCOUNTNUMBER = "UK.OBIE.SortCodeAccountNumber";
    private static final String CELLULAR = "UK.OBIE.Paym";


    public Optional<AccountReferenceType> map(String accountScheme, String identification) {
        if (StringUtils.isEmpty(accountScheme)) {
            return Optional.empty();
        }
        switch (accountScheme) {
            case IBAN:
                return Optional.of(AccountReferenceType.IBAN);
            case BBAN:
                return Optional.of(AccountReferenceType.BBAN);
            case PAN:
                return Optional.of(NumberUtils.isDigits(identification) ? AccountReferenceType.PAN : AccountReferenceType.MASKED_PAN);
            case SORTCODEACCOUNTNUMBER:
                return Optional.of(AccountReferenceType.SORTCODEACCOUNTNUMBER);
            case CELLULAR:
                return Optional.of(AccountReferenceType.MSISDN);
            default:
                return Optional.empty();
        }
    }
}
