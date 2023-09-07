package com.yolt.providers.stet.generic.dto.registration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StetTokenEndpointAuthMethod {

  NONE("none"),
  CLIENT_SECRET_POST("client_secret_post"),
  CLIENT_SECRET_BASIC("client_secret_basic"),
  TLS_CLIENT_AUTH("tls_client_auth");

  private final String value;
}