package com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegistrationRequest {

    private App app;
    private Product product;
}
