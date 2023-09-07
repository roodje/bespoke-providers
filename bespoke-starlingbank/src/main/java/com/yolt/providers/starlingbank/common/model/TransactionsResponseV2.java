package com.yolt.providers.starlingbank.common.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TransactionsResponseV2 {

    private List<FeedItemV2> feedItems = new ArrayList<>();

}
