package com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SubscriptionRequest {
    private String clientId;
    private Product product;
}
