package com.yolt.providers.openbanking.ais.santander.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.santander.dto.SantanderAccessMeansV2;
import com.yolt.providers.openbanking.ais.santander.service.ais.mappers.SantanderAccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SantanderAccessMeansStateMapperTest {
    SantanderAccessMeansStateMapper santanderAccessMeansStateMapper = new SantanderAccessMeansStateMapper (OpenBankingTestObjectMapper.INSTANCE);
    SantanderAccessMeansV2 santanderAccessMeans = new SantanderAccessMeansV2(Instant.parse("2021-06-01T08:30:17.294514600Z"),
            UUID.fromString("dfeb4e02-4f43-4e4e-8663-0226001778eb"), "accessToken","refreshToken",
            Date.from(Instant.parse("2018-01-12T12:13:14.123Z")), Date.from(Instant.parse("2018-01-11T12:13:14.123Z")), "redirectUri");
    @Test
    void shouldMapWithSantanderAccessMeansToJson() throws JsonProcessingException, TokenInvalidException {
        //when
        String json = santanderAccessMeansStateMapper.toJson(new AccessMeansState<>(santanderAccessMeans,null));
        //then
        assertThat(json).isEqualTo("{\"accessMeans\":{\"created\":\"2021-06-01T08:30:17.294514600Z\",\"userId\":\"dfeb4e02-4f43-4e4e-8663-0226001778eb\"," +
                "\"accessToken\":\"accessToken\",\"refreshToken\":\"refreshToken\",\"expireTime\":\"2018-01-12T12:13:14.123+0000\"," +
                "\"updated\":\"2018-01-11T12:13:14.123+0000\",\"redirecturi\":\"redirectUri\"}}");
    }

    @Test
    void shouldMapFromJsonToSantanderAccessMeans() throws TokenInvalidException {
        //given
        String json = "{\"accessMeans\":{\"created\":\"2021-06-01T08:30:17.294514600Z\",\"userId\":\"dfeb4e02-4f43-4e4e-8663-0226001778eb\"," +
        "\"accessToken\":\"accessToken\",\"refreshToken\":\"refreshToken\",\"expireTime\":\"2018-01-12T12:13:14.123+0000\"," +
                "\"updated\":\"2018-01-11T12:13:14.123+0000\",\"redirecturi\":\"redirectUri\"}}";
        //when
        SantanderAccessMeansV2 result = santanderAccessMeansStateMapper.fromJson(json).getAccessMeans();
        //then
        assertThat(result).isEqualToComparingFieldByField(santanderAccessMeans);
    }
}
