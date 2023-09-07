package com.yolt.providers.abnamrogroup.abnamro;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.abnamrogroup.common.AbnAmroAccessTokenMapper;
import com.yolt.providers.abnamrogroup.common.AbnAmroDataProvider;
import com.yolt.providers.abnamrogroup.common.RestTemplateSupplier;
import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthorizationService;
import com.yolt.providers.abnamrogroup.common.data.AbnAmroDataMapper;
import com.yolt.providers.abnamrogroup.common.data.AbnAmroFetchDataService;
import com.yolt.providers.abnamrogroup.common.data.TransactionTimestampDateExtractor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AisConfiguration {

    @Bean
    public AbnAmroDataProvider getAbnAmroDataProvider(final AbnAmroProperties properties,
                                                      @Value("${lovebird.abnamro.pagination-limit:1000}") final int paginationLimit,
                                                      @Qualifier("AbnAmroObjectMapper") final ObjectMapper objectMapper,
                                                      final Clock clock) {
        return new AbnAmroDataProvider(properties,
                new AbnAmroAuthorizationService(new AbnAmroAccessTokenMapper(objectMapper, clock)),
                new AbnAmroFetchDataService(paginationLimit, new AbnAmroDataMapper(
                        // TODO: Temporary implementation for C4PO-5385. Previous one: TransactionBookDateDateExtractor
                        new TransactionTimestampDateExtractor(),
                        clock)),
                new AbnAmroAccessTokenMapper(objectMapper, clock),
                new RestTemplateSupplier());
    }
}
