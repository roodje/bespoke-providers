package com.yolt.providers.openbanking.ais.hsbcgroup.ais2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansMapper;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.model.HsbcGroupAccessMeansV2;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;

class HsbcAccessMeansMapperTest {

    @Test
    void shouldMapWithRedirectUri() throws JsonProcessingException, TokenInvalidException {
        HsbcGroupAccessMeansV2 dummyWithRedirectUri = new HsbcGroupAccessMeansV2(
                Instant.now(),
                UUID.randomUUID(),
                "any",
                "any",
                Date.from(Instant.now().plus(1, DAYS)),
                Date.from(Instant.now()),
                "https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0");
        // when
        AccessMeans accessMeans = new DefaultAccessMeansMapper<>(OpenBankingTestObjectMapper.INSTANCE, HsbcGroupAccessMeansV2.class)
                .fromJson(OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(dummyWithRedirectUri));

        // then
        assertThat(accessMeans.getRedirectUri()).isEqualTo("https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0");
    }

}