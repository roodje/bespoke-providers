package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultAccessMeansStateMapperTest {
    private final ObjectMapper objectMapper = OpenBankingTestObjectMapper.INSTANCE;
    private final AccessMeansStateMapper<AccessMeansState> accessMeansStateMapper = new DefaultAccessMeansStateMapper<AccessMeansState>(objectMapper);
    private final String accessMeansStateAsJson = """
            {"accessMeans":{"created":"1995-10-22T00:00:00Z","userId":"11100000-1111-1111-1111-010aaa1aa00a",\
            "accessToken":"accessToken","refreshToken":"refreshToken","expireTime":"1995-10-24T00:00:00.000+0000",\
            "updated":"1995-10-23T00:00:00.000+0000","redirectUri":"redirectUri"},"permissions":["ReadParty"]}\
            """;

    @Test
    public void shouldMapAccessMeansStateToJson() {
        //given
        String userId = "11100000-1111-1111-1111-10aaa1aa00a";
        AccessMeans accessMeans = new AccessMeans(Instant.parse("1995-10-22T00:00:00Z"), UUID.fromString(userId), "accessToken", "refreshToken",
                Date.from(Instant.parse("1995-10-24T00:00:00Z")),
                Date.from(Instant.parse("1995-10-23T00:00:00Z")),
                "redirectUri");
        AccessMeansState accessMeansState = new AccessMeansState(accessMeans, List.of("ReadParty"));
        //when
        String json = accessMeansStateMapper.toJson(accessMeansState);
        //then
        assertThat(json).isEqualTo(accessMeansStateAsJson);

    }

    @Test
    public void shouldMapAccessMeansStateFromJson() throws TokenInvalidException {
        //given
        String json = accessMeansStateAsJson;
        String userId = "11100000-1111-1111-1111-10aaa1aa00a";
        AccessMeans expectedAccessMeans = new AccessMeans(Instant.parse("1995-10-22T00:00:00Z"), UUID.fromString(userId), "accessToken", "refreshToken",
                Date.from(Instant.parse("1995-10-24T00:00:00Z")),
                Date.from(Instant.parse("1995-10-23T00:00:00Z")),
                "redirectUri");
        //when
        AccessMeansState state = accessMeansStateMapper.fromJson(json);
        //then
        assertThat(state).isEqualTo(new AccessMeansState(expectedAccessMeans, List.of("ReadParty")));
    }
}
