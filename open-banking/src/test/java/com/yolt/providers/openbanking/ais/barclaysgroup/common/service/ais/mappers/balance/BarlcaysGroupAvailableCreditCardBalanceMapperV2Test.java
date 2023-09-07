package com.yolt.providers.openbanking.ais.barclaysgroup.common.service.ais.mappers.balance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.barclaysgroup.barclays.beanconfig.BarclaysObjectMapperBean;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.BalanceMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataBalance;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class BarlcaysGroupAvailableCreditCardBalanceMapperV2Test {

    final static private String PREFIX = "__files/barclaysgroup/common/service/ais/mappers/balance/";

    private BalanceMapper balanceMapper = new BarlcaysGroupAvailableCreditCardBalanceMapperV2();
    private ObjectMapper objectMapper = new BarclaysObjectMapperBean().getBarclaysObjectMapper(new Jackson2ObjectMapperBuilder());

    @Test
    public void shouldReturnInterimbookedBalanceFromCreditLineTypeAvailableScenarioOne() throws Exception {
        // expectation
        BigDecimal expectedBalance = new BigDecimal("6309.53");

        // given
        String interimBookedBalancePath = "account1/interimBookedDebitBalance.json";
        String interimClearedBalancePath = "account1/interimClearedCreditBalance.json";

        Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType = new HashMap<>();
        balancesByType.put(INTERIMBOOKED, readJsonToOBReadBalance1DataBalanceMapper(interimBookedBalancePath));
        balancesByType.put(INTERIMCLEARED, readJsonToOBReadBalance1DataBalanceMapper(interimClearedBalancePath));
        Supplier<List<OBBalanceType1Code>> balanceListTypeSupplier = () -> Collections.singletonList(INTERIMBOOKED);

        // when
        BigDecimal actualBalance = balanceMapper.getBalance(balancesByType, balanceListTypeSupplier);

        // then
        assertThat(actualBalance).isEqualTo(expectedBalance);
    }

    @Test
    public void shouldReturnInterimbookedBalanceFromCreditLineTypeAvailableScenarioTwo() throws Exception {
        // expectation
        BigDecimal expectedBalance = new BigDecimal("6650.00");

        // given
        String interimBookedBalancePath = "account2/interimBookedCreditBalance.json";
        String interimClearedBalancePath = "account2/interimClearedCreditBalance.json";

        Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType = new HashMap<>();
        balancesByType.put(INTERIMBOOKED, readJsonToOBReadBalance1DataBalanceMapper(interimBookedBalancePath));
        balancesByType.put(INTERIMCLEARED, readJsonToOBReadBalance1DataBalanceMapper(interimClearedBalancePath));
        Supplier<List<OBBalanceType1Code>> balanceListTypeSupplier = () -> Collections.singletonList(INTERIMBOOKED);

        // when
        BigDecimal actualBalance = balanceMapper.getBalance(balancesByType, balanceListTypeSupplier);

        // then
        assertThat(actualBalance).isEqualTo(expectedBalance);
    }

    @Test
    public void shouldReturnInterimbookedBalanceFromCreditLineTypeCreditAndAmount() throws Exception {
        // expectation
        BigDecimal expectedBalance = new BigDecimal("6309.53");

        // given
        String interimBookedBalancePath = "account4/interimBookedDebitBalance.json";

        Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType = new HashMap<>();
        balancesByType.put(INTERIMBOOKED, readJsonToOBReadBalance1DataBalanceMapper(interimBookedBalancePath));
        Supplier<List<OBBalanceType1Code>> balanceListTypeSupplier = () -> Collections.singletonList(INTERIMBOOKED);

        // when
        BigDecimal actualBalance = balanceMapper.getBalance(balancesByType, balanceListTypeSupplier);

        // then
        assertThat(actualBalance).isEqualTo(expectedBalance);
    }

    @Test
    public void shouldReturnInterimbookedBalancefromAmount() throws Exception {
        // expectation
        BigDecimal expectedBalance = new BigDecimal("-190.47");

        // given
        String interimBookedBalancePath = "account5/interimBookedDebitBalance.json";

        Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType = new HashMap<>();
        balancesByType.put(INTERIMBOOKED, readJsonToOBReadBalance1DataBalanceMapper(interimBookedBalancePath));
        Supplier<List<OBBalanceType1Code>> balanceListTypeSupplier = () -> Collections.singletonList(INTERIMBOOKED);

        // when
        BigDecimal actualBalance = balanceMapper.getBalance(balancesByType, balanceListTypeSupplier);

        // then
        assertThat(actualBalance).isEqualTo(expectedBalance);
    }

    @Test
    public void shouldReturnNullForNoTypeMatching() throws Exception {
        // given
        String interimBookedBalancePath = "account3/expectedDebit.json";

        Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType = new HashMap<>();
        balancesByType.put(EXPECTED, readJsonToOBReadBalance1DataBalanceMapper(interimBookedBalancePath));
        Supplier<List<OBBalanceType1Code>> balanceListTypeSupplier = () -> Collections.singletonList(INTERIMBOOKED);

        // when
        BigDecimal actualBalance = balanceMapper.getBalance(balancesByType, balanceListTypeSupplier);

        // then
        assertThat(actualBalance).isNull();
    }


    private OBReadBalance1DataBalance readJsonToOBReadBalance1DataBalanceMapper(final String jsonPath) throws Exception {
        String jsonString = readFile(PREFIX + jsonPath);
        return objectMapper.readValue(jsonString, OBReadBalance1DataBalance.class);
    }

    private static String readFile(String filename) throws Exception {
        Path filePath = new File(Objects.requireNonNull(BarlcaysGroupAvailableCreditCardBalanceMapperV2Test.class.getClassLoader()
                .getResource(filename)).toURI()).toPath();
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }
}