package com.yolt.providers.stet.bnpparibasgroup.mapper;

import com.yolt.providers.stet.bnpparibasgroup.common.mapper.fetchdata.BnpParibasGroupBalanceMapper;
import com.yolt.providers.stet.generic.dto.TestStetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static com.yolt.providers.stet.generic.dto.balance.StetBalanceType.*;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class BnpParibasGroupBalanceMapperTest {
    private static final String SUPPORTED_CURRENCY = "EUR";
    private static final BigDecimal AMOUNT_10 = BigDecimal.valueOf(10.00);
    private static final BigDecimal AMOUNT_20 = BigDecimal.valueOf(20.00);
    private static final BigDecimal AMOUNT_30 = BigDecimal.valueOf(30.00);
    private static final CurrencyCode CURRENCY_CODE = CurrencyCode.valueOf(SUPPORTED_CURRENCY);
    private static final BnpParibasGroupBalanceMapper balanceMapper = new BnpParibasGroupBalanceMapper();

    @Test
    public void shouldExtractBalanceAmountCLBD() {
        //given
        List<StetBalanceDTO> balanceResources = prepareBalanceList();
        //when
        BigDecimal amount = balanceMapper.extractBalanceAmount(balanceResources, CURRENCY_CODE, List.of(CLBD));
        //then
        assertThat(amount).isEqualTo("10.0");
    }

    @Test
    public void shouldExtractBalanceAmountXPCD() {
        //given
        List<StetBalanceDTO> balanceResources = prepareBalanceList();
        //when
        BigDecimal amount = balanceMapper.extractBalanceAmount(balanceResources, CURRENCY_CODE, List.of(XPCD));
        //then
        assertThat(amount).isEqualTo("20.0");
    }

    @Test
    public void shouldExtractBalanceAmountOTHR() {
        //given
        List<StetBalanceDTO> balanceResources = prepareBalanceList();
        //when
        BigDecimal amount = balanceMapper.extractBalanceAmount(balanceResources, CURRENCY_CODE, List.of(OTHR));
        //then
        assertThat(amount).isEqualTo("30.0");
    }

    @Test
    public void shouldExtractBalanceAmountWithDuplicatedBalance() {
        //given
        List<StetBalanceDTO> balanceResources = prepareBalanceList();

        //when
        BigDecimal amount = balanceMapper.extractBalanceAmount(balanceResources, CURRENCY_CODE, List.of(CLBD));
        //then
        assertThat(amount).isEqualTo("10.0");
    }

    @Test
    public void shouldReturnNullWhenMandatoryXPCDBalanceIsMissing() {
        //given
        List<StetBalanceDTO> balanceResources = prepareBalanceList();
        //then
        BigDecimal balance = balanceMapper.extractBalanceAmount(balanceResources, CURRENCY_CODE, List.of(XPCD));
        //when
        assertThat(balance).isEqualTo("20.0");
    }

    @Test
    public void shouldReturnNullWhenBalanceResourceAreEmpty() {
        //given
        List<StetBalanceDTO> balanceResources = new ArrayList<>();
        //when
        BigDecimal balance = balanceMapper.extractBalanceAmount(balanceResources, CURRENCY_CODE, List.of(OTHR));
        //then
        assertThat(balance).isNull();
    }

    @Test
    public void shouldExtractBalanceAmountWithReturnNullOfEmptyBalanceResources() {
        //given
        List<StetBalanceDTO> balances = emptyList();
        //when
        BigDecimal balance = balanceMapper.extractBalanceAmount(balances, CURRENCY_CODE, List.of(OTHR));
        //then
        assertThat(balance).isNull();
    }

    @Test
    public void shouldMapToBalancesDtoCorrectly() {
        //given
        List<StetBalanceDTO> balanceResources = prepareBalanceList();
        //when
        List<BalanceDTO> balanceDTOs = balanceMapper.mapToBalanceDTOs(balanceResources, CURRENCY_CODE);
        //then
        assertThat(balanceDTOs).hasSize(2);

        BalanceDTO firstBalanceDTO = balanceDTOs.get(0);
        assertThat(firstBalanceDTO.getBalanceAmount().getAmount()).isEqualTo("10.0");
        assertThat(firstBalanceDTO.getBalanceType()).isEqualTo(BalanceType.CLOSING_BOOKED);

        BalanceDTO secondBalanceDTO = balanceDTOs.get(1);
        assertThat(secondBalanceDTO.getBalanceAmount().getAmount()).isEqualTo("20.0");
        assertThat(secondBalanceDTO.getBalanceType()).isEqualTo(BalanceType.INTERIM_AVAILABLE);
    }

    @Test
    public void shouldMapToBalanceDTOsWithEmptyBalanceResources() {
        //given
        List<StetBalanceDTO> balances = emptyList();
        //when
        List<BalanceDTO> balanceDTOs = balanceMapper.mapToBalanceDTOs(balances, CURRENCY_CODE);
        //then
        assertThat(balanceDTOs).isEmpty();
    }

    @Test
    public void shouldReturnNullWhenBalanceResourceAreEmptyAndListOfPrefferedBalancesArePassed() {
        // given
        List<StetBalanceDTO> balanceResources = new ArrayList<>();
        // when
        BigDecimal balance = balanceMapper.extractBalanceAmount(balanceResources, CURRENCY_CODE, List.of(OTHR, XPCD));

        // then
        assertThat(balance).isNull();
    }

    private StetBalanceDTO createBalanceResource(StetBalanceType balanceType, BigDecimal amount) {
        return TestStetBalanceDTO.builder()
                .name("Solde comptable au 12/01/2017")
                .amount(amount)
                .currency(CURRENCY_CODE)
                .type(balanceType)
                .referenceDate(OffsetDateTime.of(LocalDateTime
                                .of(2019, 7, 1, 0, 0),
                        ZoneOffset.ofHoursMinutes(0, 0)))
                .build();
    }

    private List<StetBalanceDTO> prepareBalanceList() {
        return List.of(
                createBalanceResource(CLBD, AMOUNT_10),
                createBalanceResource(XPCD, AMOUNT_20),
                createBalanceResource(OTHR, AMOUNT_30));
    }

}
