package com.yolt.providers.openbanking.ais.hsbcgroup.ais2;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.model.HsbcGroupAccessMeansV2;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.model.mapper.HsbcAccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class HsbcAccessMeansStateMapperTest {

    HsbcAccessMeansStateMapper accessMeansStateMapper = new HsbcAccessMeansStateMapper(OpenBankingTestObjectMapper.INSTANCE);

    HsbcGroupAccessMeansV2 hsbcGroupAccessMeansV2 = new HsbcGroupAccessMeansV2(
            Instant.parse("2021-06-01T08:30:17.294514600Z"),
            UUID.fromString("bca1e25b-9d24-4490-9e58-fc9927d6ce2c"),
            "any",
            "any",
            Date.from(Instant.parse("2021-06-01T08:30:17.294514600Z")),
            Date.from(Instant.parse("2021-06-01T08:30:17.294514600Z")),
            "https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0");

    @Test
    void shouldMapHsbcAccessMeansToJsonWithoutPermissions() {
        //given
        AccessMeansState<HsbcGroupAccessMeansV2> accessMeansState = new AccessMeansState<>(hsbcGroupAccessMeansV2, Collections.emptyList());
        //when
        String json = accessMeansStateMapper.toJson(accessMeansState);
        //then
        assertThat(json).isEqualTo("""
                {"accessMeans":\
                {"created":"2021-06-01T08:30:17.294514600Z",\
                "userId":"bca1e25b-9d24-4490-9e58-fc9927d6ce2c",\
                "accessToken":"any",\
                "refreshToken":"any",\
                "expireTime":"2021-06-01T08:30:17.294+0000",\
                "updated":"2021-06-01T08:30:17.294+0000",\
                "redirecturi":"https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0"},\
                "permissions":[]\
                }\
                """);
    }

    @Test
    void shouldMapHsbcAccessMeansToJsonWithPermissions() {
        //given
        AccessMeansState<HsbcGroupAccessMeansV2> accessMeansState = new AccessMeansState<>(hsbcGroupAccessMeansV2, List.of("ReadParty"));
        //when
        String json = accessMeansStateMapper.toJson(accessMeansState);
        //then
        assertThat(json).isEqualTo("""
                {"accessMeans":\
                {"created":"2021-06-01T08:30:17.294514600Z",\
                "userId":"bca1e25b-9d24-4490-9e58-fc9927d6ce2c",\
                "accessToken":"any",\
                "refreshToken":"any",\
                "expireTime":"2021-06-01T08:30:17.294+0000",\
                "updated":"2021-06-01T08:30:17.294+0000",\
                "redirecturi":"https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0"},\
                "permissions":["ReadParty"]\
                }\
                """);
    }

    @Test
    void shouldMapJsonToHsbcAccessMeansWithoutPermissions() throws TokenInvalidException {
        //given
        String json = """
                {"accessMeans":\
                {"created":"2021-06-01T08:30:17.294514600Z",\
                "userId":"bca1e25b-9d24-4490-9e58-fc9927d6ce2c",\
                "accessToken":"any",\
                "refreshToken":"any",\
                "expireTime":"2021-06-01T08:30:17.294+0000",\
                "updated":"2021-06-01T08:30:17.294+0000",\
                "redirecturi":"https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0"},\
                "permissions":[]\
                }\
                """;
        AccessMeansState<HsbcGroupAccessMeansV2> expectedAccessMeansState = new AccessMeansState<>(hsbcGroupAccessMeansV2, Collections.emptyList());
        //when
        AccessMeansState<HsbcGroupAccessMeansV2> receivedAccessMeansState = accessMeansStateMapper.fromJson(json);
        //then
        assertThat(receivedAccessMeansState).isEqualToComparingFieldByField(expectedAccessMeansState);
    }

    @Test
    void shouldMapJsonToHsbcAccessMeansWithPermissions() throws TokenInvalidException {
        //given
        String json = """
                {"accessMeans":\
                {"created":"2021-06-01T08:30:17.294514600Z",\
                "userId":"bca1e25b-9d24-4490-9e58-fc9927d6ce2c",\
                "accessToken":"any",\
                "refreshToken":"any",\
                "expireTime":"2021-06-01T08:30:17.294+0000",\
                "updated":"2021-06-01T08:30:17.294+0000",\
                "redirecturi":"https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0"},\
                "permissions":["ReadParty"]\
                }\
                """;
        AccessMeansState<HsbcGroupAccessMeansV2> expectedAccessMeansState = new AccessMeansState<>(hsbcGroupAccessMeansV2, List.of("ReadParty"));
        //when
        AccessMeansState<HsbcGroupAccessMeansV2> receivedAccessMeansState = accessMeansStateMapper.fromJson(json);
        //then
        assertThat(receivedAccessMeansState).isEqualToComparingFieldByField(expectedAccessMeansState);
    }
}
