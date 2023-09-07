package com.yolt.providers.stet.generic.dto.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StetKeyDTO {

  private String kty;
  private String crv;
  private String x;
  private String y;
  private String use;
  private String kid;
  private String n;
  private String e;
  private String alg;
  private String x5u;
  private String x5c;
  private String x5t;

  @JsonProperty("key_ops")
  private String keyOps;

  @JsonProperty("x5t#s256")
  private String x5ts256;
}