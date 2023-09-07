package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultAccessMeanMapperTest {
    private final ObjectMapper objectMapper = OpenBankingTestObjectMapper.INSTANCE;
    private final AccessMeansMapper<AccessMeans> accessMeansMapper = new DefaultAccessMeansMapper<>(objectMapper);
    private final String accessMeansAsJson = """
            {"created":"1995-10-22T00:00:00Z","userId":"11100000-1111-1111-1111-010aaa1aa00a","accessToken":"accessToken","refreshToken":"refreshToken",\
            "expireTime":"1995-10-24T00:00:00.000+0000","updated":"1995-10-23T00:00:00.000+0000","redirectUri":"redirectUri"}\
            """;

    @Test
    public void shouldMapAccessMeansStateToJson() {
        //given
        String userId = "11100000-1111-1111-1111-10aaa1aa00a";
        AccessMeans accessMeans = new AccessMeans(Instant.parse("1995-10-22T00:00:00Z"), UUID.fromString(userId), "accessToken", "refreshToken",
                Date.from(Instant.parse("1995-10-24T00:00:00Z")),
                Date.from(Instant.parse("1995-10-23T00:00:00Z")),
                "redirectUri");
        //when
        String json = accessMeansMapper.toJson(accessMeans);
        //then
        assertThat(json).isEqualTo(accessMeansAsJson);

    }

    @Test
    public void shouldMapAccessMeansStateFromJson() throws TokenInvalidException {
        //given
        String json = accessMeansAsJson;
        String userId = "11100000-1111-1111-1111-10aaa1aa00a";
        AccessMeans expectedAccessMeans = new AccessMeans(Instant.parse("1995-10-22T00:00:00Z"), UUID.fromString(userId), "accessToken", "refreshToken",
                Date.from(Instant.parse("1995-10-24T00:00:00Z")),
                Date.from(Instant.parse("1995-10-23T00:00:00Z")),
                "redirectUri");
        //when
        AccessMeans accessMeans = accessMeansMapper.fromJson(json);
        //then
        assertThat(accessMeans).isEqualTo(expectedAccessMeans);
    }
}
