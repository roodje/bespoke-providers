package com.yolt.providers.openbanking.ais.aibgroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class AibGroupObjectMapperTest {

    private final ObjectMapper objectMapper = new OpenbankingConfiguration().getObjectMapper();

    @Test
    public void shouldDeserializeTokenWithUnknownFields() throws IOException {
        // given
        String expectedAccessToken = "at12345";
        String expectedRefreshToken = "rt2345";
        String expectedExpireTime = "2018-01-11T12:13:14.123Z";
        String input = String.format(
                "{\"unknownField\": null, \"unknownField2\": null, \"accessToken\": \"%s\", \"refreshToken\": \"%s\", \"expireTime\": \"%s\"}",
                expectedAccessToken,
                expectedRefreshToken,
                expectedExpireTime);

        // when
        AccessMeans output = objectMapper.readValue(input, AccessMeans.class);

        // then
        assertThat(output.getAccessToken()).isEqualTo(expectedAccessToken);
        assertThat(output.getRefreshToken()).isEqualTo(expectedRefreshToken);
        assertThat(output.getExpireTime()).isEqualTo(Date.from(Instant.parse(expectedExpireTime)));
    }
}
