package com.yolt.providers.belfius.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BelfiusGroupProviderState {
    private String language;
    private String codeVerifier;
}
