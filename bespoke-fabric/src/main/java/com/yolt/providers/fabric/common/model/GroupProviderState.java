package com.yolt.providers.fabric.common.model;

import lombok.Value;

import java.time.LocalDate;

@Value
public class GroupProviderState {
    String consentId;
    Long consentGeneratedAt;
    LocalDate consentValidTo;
}
