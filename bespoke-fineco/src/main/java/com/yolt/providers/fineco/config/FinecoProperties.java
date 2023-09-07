package com.yolt.providers.fineco.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Component
@ConfigurationProperties("lovebird.fineco")
@Validated
public class FinecoProperties {

    @NotEmpty
    private String baseUrl;
    @NotEmpty
    private String consentUrl;
    @NotNull
    private Long transactionsTimeFrameDays;
    @NotNull
    private Long transactionsTimeFrameMinutes;
    @NotNull
    private AccountsEndpoints currentAccounts;
    @NotNull
    private AccountsEndpoints cardAccounts;
    @NotNull
    private PaymentsEndpoints sepaPayments;
    @Min(1)
    private int paginationLimit;

    @Data
    @Validated
    public static class AccountsEndpoints {
        @NotNull
        private String accountsUrl;
        @NotNull
        private String transactionsUrlTemplate;
        @NotNull
        private String balancesUrlTemplate;
    }

    @Data
    @Validated
    public static class PaymentsEndpoints {
        @NotNull
        private String paymentInitiation;
        @NotNull
        private String paymentStatus;
    }
}
