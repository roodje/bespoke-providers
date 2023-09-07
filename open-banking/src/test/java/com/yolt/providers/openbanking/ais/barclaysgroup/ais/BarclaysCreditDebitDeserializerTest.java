package com.yolt.providers.openbanking.ais.barclaysgroup.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.barclaysgroup.BarclaysApp;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataBalance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {BarclaysApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("barclays")
public class BarclaysCreditDebitDeserializerTest {
    @Autowired
    @Qualifier("BarclaysObjectMapperV2")
    private ObjectMapper objectMapper;

    @Test
    public void shouldMapWhenNormalAnswerComesIn() throws JsonProcessingException {
        //given
        String aBalnceJson = "{\n" +
                "            \"AccountId\": \"20000000000001449160\",\n" +
                "            \"Amount\": {\n" +
                "              \"Amount\": \"11.12\",\n" +
                "              \"Currency\": \"GBP\"\n" +
                "            },\n" +
                "            \"CreditDebitIndicator\": \"Credit\",\n" +
                "            \"DateTime\": \"2020-12-01T07:35:55Z\",\n" +
                "            \"Type\": \"ClosingBooked\"\n" +
                "          }";
        //when
        OBReadBalance1DataBalance value = objectMapper.readValue(aBalnceJson, OBReadBalance1DataBalance.class);
        //then
        assertThat(value.getCreditDebitIndicator()).isNotNull();
    }

    @Test
    public void shouldMapItCaseInsensitive() throws JsonProcessingException {
        //given
        String aBalnceJson = "{\n" +
                "            \"AccountId\": \"20000000000001449160\",\n" +
                "            \"Amount\": {\n" +
                "              \"Amount\": \"11.12\",\n" +
                "              \"Currency\": \"GBP\"\n" +
                "            },\n" +
                "            \"CreditDebitIndicator\": \"CrEdIT\",\n" +
                "            \"DateTime\": \"2020-12-01T07:35:55Z\",\n" +
                "            \"Type\": \"ClosingBooked\"\n" +
                "          }";
        //when
        OBReadBalance1DataBalance value = objectMapper.readValue(aBalnceJson, OBReadBalance1DataBalance.class);
        //then
        assertThat(value.getCreditDebitIndicator()).isNotNull();
    }

}
