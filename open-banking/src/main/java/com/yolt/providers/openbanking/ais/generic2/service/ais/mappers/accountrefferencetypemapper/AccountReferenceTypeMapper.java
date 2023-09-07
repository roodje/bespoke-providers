package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper;

import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;

import java.util.Optional;

public interface AccountReferenceTypeMapper {

    Optional<AccountReferenceType> map(String accountScheme, String identification);
}
