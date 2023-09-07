package com.yolt.providers.stet.cicgroup.common.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TokenResponse {

  private String accessToken;
  private String tokenType;
  private BigDecimal expiresIn;
  private String refreshToken;
  private String scope;
  private boolean refreshed;
}

