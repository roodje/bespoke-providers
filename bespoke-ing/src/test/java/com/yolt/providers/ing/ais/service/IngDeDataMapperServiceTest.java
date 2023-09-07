package com.yolt.providers.ing.ais.service;

import com.yolt.providers.ing.common.dto.TestTransaction;
import com.yolt.providers.ing.de.service.IngDeDataMapperService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.ZoneId;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class IngDeDataMapperServiceTest {

    private IngDeDataMapperService ingDeDataMapperService = new IngDeDataMapperService(ZoneId.of("Europe/Berlin"));

    public static Stream<Arguments> provideDescriptionsAndResults() {
        return Stream.of(
                arguments("mandatereference:,creditorid:,remittanceinformation:NR XXXX", "NR XXXX"),
                arguments("mandatereference:101407,creditorid:DE16RPA00000099999,remittanceinformation:REWE SAGT DANKE. 44999999//Florstadt/DE 2021-08-12T10:13:17 Folgenr.000 Verfalld.2022-12 Auszahl. 99,99EUR",
                        "REWE SAGT DANKE. 44999999//Florstadt/DE 2021-08-12T10:13:17 Folgenr.000 Verfalld.2022-12 Auszahl. 99,99EUR"),
                arguments("remittanceinformation:", "N/A"),
                arguments("random transaction description", "random transaction description"),
                arguments(" ", "N/A"),
                arguments("", "N/A"),
                arguments(null, "N/A"),
                arguments("random<br>transaction description", "random\ntransaction description"),
                arguments("mandatereference:,creditorid:,remittanceinformation:NR<br>XXXX", "NR\nXXXX")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDescriptionsAndResults")
    public void shouldReturnExtractedTransactionDescription(String description, String expected) {
        // when
        String result = ingDeDataMapperService.retrieveSanitizedTransactionDescription(TestTransaction.builder()
                .description(description)
                .build());

        // then
        assertThat(result).isEqualTo(expected);
    }
}


