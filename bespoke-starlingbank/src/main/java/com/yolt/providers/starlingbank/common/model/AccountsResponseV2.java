package com.yolt.providers.starlingbank.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountsResponseV2 {

    @Builder.Default
    List<AccountV2> accounts = new ArrayList<>();
}
