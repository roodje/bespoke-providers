package com.yolt.providers.stet.bnpparibasgroup.common.onboarding;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonWebKey {

    private String alg;
    private String kty;
    private String use;
    private List<String> x5c;
    private String n;
    private String e;
    private String kid;
    private String x5t;
    private String x5u;
}
