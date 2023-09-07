package com.yolt.providers.unicredit.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequestDTO {
    private String userEmail;
}
