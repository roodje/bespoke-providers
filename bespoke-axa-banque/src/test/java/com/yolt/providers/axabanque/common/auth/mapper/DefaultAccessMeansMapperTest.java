package com.yolt.providers.axabanque.common.auth.mapper;

import com.yolt.providers.axabanque.common.auth.mapper.access.AccessMeansMapper;
import com.yolt.providers.axabanque.common.auth.mapper.access.DefaultAccessMeansMapper;
import com.yolt.providers.axabanque.common.model.internal.AccessToken;
import com.yolt.providers.axabanque.common.model.internal.GroupAccessMeans;
import com.yolt.providers.axabanque.common.model.internal.GroupProviderState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultAccessMeansMapperTest {

    private AccessMeansMapper accessMeansMapper;

    @BeforeEach
    public void setup() {
        accessMeansMapper = new DefaultAccessMeansMapper();
    }

    @Test
    public void shouldReturnAxaAccessMeans() {
        //given
        Instant dummyDate = Instant.parse("1385-08-14T18:35:24.00Z");
        AccessToken accessToken = new AccessToken(123L, "refreshToken", "scope", "tokenType", "accessToken");
        GroupProviderState providerState = new GroupProviderState("codeVerifier", "code", "consentId",
                "xTraceId", dummyDate.toEpochMilli());
        GroupAccessMeans expected = new GroupAccessMeans("redirectUri", providerState, accessToken);

        //when
        GroupAccessMeans axaAccessMeans = accessMeansMapper.mapToAccessMeans("redirectUri", providerState, accessToken);
        //then
        assertThat(axaAccessMeans).isEqualToComparingFieldByField(expected);
    }
}
