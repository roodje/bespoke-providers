package com.yolt.providers.openbanking.ais.tescobank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TescoSampleAccessMeansV2 {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static final ObjectMapper OBJECT_MAPPER = OpenBankingTestObjectMapper.INSTANCE;

    public static String getSerializedAccessMeans() throws JsonProcessingException {
        AccessMeansState<AccessMeans> token = new AccessMeansState<>(new AccessMeans(
                Instant.now(),
                USER_ID,
                ACCESS_TOKEN,
                REFRESH_TOKEN,
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL),
                List.of(
                        "ReadAccountsDetail",
                        "ReadBalances",
                        "ReadDirectDebits",
                        "ReadProducts",
                        "ReadStandingOrdersDetail",
                        "ReadTransactionsCredits",
                        "ReadTransactionsDebits",
                        "ReadTransactionsDetail"));
        return OBJECT_MAPPER.writeValueAsString(token);
    }
}
