package com.yolt.providers.starlingbank.common.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedItemV2 {

    String feedItemUid;
    String categoryUid;
    CurrencyAndAmountV2 amount;
    CurrencyAndAmountV2 sourceAmount;
    FeedItemDirection direction;
    String updatedAt;
    String transactionTime;
    String settlementTime;
    String source;
    String sourceSubType;
    TransactionStatusV2 status;
    String counterPartyType;
    String counterPartyUid;
    String counterPartyName;
    String counterPartySubEntityUid;
    String counterPartySubEntityName;
    String counterPartySubEntityIdentifier;
    String counterPartySubEntitySubIdentifier;
    String reference;
    String country;
    String spendingCategory;
    String userNote;
    RoundUpV2 roundUp;
    private static class RoundUpV2 {
        String goalCategoryUid;
        CurrencyAndAmountV2 amount;
    }
}
