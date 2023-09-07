package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.DefaultAccountReferenceTypeMapper;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultAccountReferenceTypeMapperTest {

    DefaultAccountReferenceTypeMapper defaultTransactionMapper = new DefaultAccountReferenceTypeMapper();

    @Test
    void shouldReturnAccountReferenceTypeWithIban() {
        //given
        String accountScheme = "UK.OBIE.IBAN";
        String accountNumber = "134566";

        //when
        Optional<AccountReferenceType> mappedAccountReference = defaultTransactionMapper.map(accountScheme, accountNumber);

        //then
        assertThat(mappedAccountReference).contains(AccountReferenceType.IBAN);
    }

    @Test
    void shouldReturnAccountReferenceTypeWithSortCodeAccountNumber() {
        //given
        String accountScheme = "UK.OBIE.SortCodeAccountNumber";
        String accountNumber = "134566";

        //when
        Optional<AccountReferenceType> mappedAccountReference = defaultTransactionMapper.map(accountScheme, accountNumber);

        //then
        assertThat(mappedAccountReference).contains(AccountReferenceType.SORTCODEACCOUNTNUMBER);
    }

    @Test
    void shouldReturnAccountReferenceTypeWithPan() {
        //given
        String accountScheme = "UK.OBIE.PAN";
        String accountNumber = "134566";

        //when
        Optional<AccountReferenceType> mappedAccountReference = defaultTransactionMapper.map(accountScheme, accountNumber);

        //then
        assertThat(mappedAccountReference).contains(AccountReferenceType.PAN);
    }

    @Test
    void shouldReturnAccountReferenceTypeWithMaskedPan() {
        //given
        String accountScheme = "UK.OBIE.PAN";
        String accountNumber = "13XXX66";

        //then
        Optional<AccountReferenceType> mappedAccountReference = defaultTransactionMapper.map(accountScheme, accountNumber);

        //then
        assertThat(mappedAccountReference).contains(AccountReferenceType.MASKED_PAN);
    }

    @Test
    void shouldReturnAccountReferenceTypeWithBban() {
        //given
        String accountScheme = "UK.OBIE.BBAN";
        String accountNumber = "13XXX66";

        //then
        Optional<AccountReferenceType> mappedAccountReference = defaultTransactionMapper.map(accountScheme, accountNumber);

        //then
        assertThat(mappedAccountReference).contains(AccountReferenceType.BBAN);
    }

    @Test
    void shouldReturnAccountReferenceTypeWithEmptyType() {
        //given
        String accountScheme = "Unknown";
        String accountNumber = "13XXX66";

        //then
        Optional<AccountReferenceType> mappedAccountReference = defaultTransactionMapper.map(accountScheme, accountNumber);

        //then
        assertThat(mappedAccountReference).isEmpty();
    }
}
