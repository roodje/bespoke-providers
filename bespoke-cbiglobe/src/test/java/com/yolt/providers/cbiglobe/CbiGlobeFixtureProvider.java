package com.yolt.providers.cbiglobe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.exception.CbiGlobeMalformedObjectException;
import com.yolt.providers.cbiglobe.common.model.CbiGlobeAccessMeansDTO;
import nl.ing.lovebird.extendeddata.account.*;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;

public class CbiGlobeFixtureProvider {

    private static final UUID USER_ID = UUID.fromString("7bb49dfa-5d54-43b0-9a39-e4b2b0e9ae10");

    public static ProviderAccountDTO createProviderAccountDTO(String accountId) {
        BalanceAmountDTO balanceAmountDTO = BalanceAmountDTO.builder()
                .currency(CurrencyCode.EUR)
                .amount(new BigDecimal("30.30"))
                .build();

        BalanceDTO balanceDTO = BalanceDTO.builder()
                .balanceType(BalanceType.INTERIM_AVAILABLE)
                .balanceAmount(balanceAmountDTO)
                .build();

        ExtendedAccountDTO extendedAccountDTO = ExtendedAccountDTO.builder()
                .status(Status.ENABLED)
                .usage(UsageType.PRIVATE)
                .product("Conto bancario corrente")
                .currency(CurrencyCode.EUR)
                .name("Il mio conto bancario")
                .balances(Collections.singletonList(balanceDTO))
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .build();

        return ProviderAccountDTO.builder()
                .lastRefreshed(ZonedDateTime.now(ZoneId.of("UTC")))
                .yoltAccountType(CURRENT_ACCOUNT)
                .availableBalance(new BigDecimal("500.10"))
                .currentBalance(new BigDecimal("600.10"))
                .accountId(accountId)
                .accountMaskedIdentification("492500******1234")
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "IT35 5000 0000 0549 1000 0003"))
                .bic("BIGBITPW")
                .name("Il mio conto bancario")
                .currency(CurrencyCode.EUR)
                .closed(false)
                .extendedAccount(extendedAccountDTO)
                .build();
    }

    public static AccessMeansDTO createAccessMeansDTO(CbiGlobeAccessMeansDTO accessMeansDTO, ObjectMapper mapper) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Warsaw"));
        String providerState = toProviderState(accessMeansDTO, mapper);
        return new AccessMeansDTO(USER_ID, providerState, Date.from(now.plusDays(7).toInstant()), Date.from(now.plusDays(14).toInstant()));
    }

    public static String toProviderState(CbiGlobeAccessMeansDTO cbiGlobeAccessMeansDTO, ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(cbiGlobeAccessMeansDTO);
        } catch (JsonProcessingException e) {
            throw new CbiGlobeMalformedObjectException("Error creating json access means");
        }
    }

    public static CbiGlobeAccessMeansDTO fromProviderState(String providerState, ObjectMapper mapper) {
        try {
            return mapper.readValue(providerState, CbiGlobeAccessMeansDTO.class);
        } catch (IOException e) {
            throw new CbiGlobeMalformedObjectException("Error reading Poste Italiane Access Means");
        }
    }
}
