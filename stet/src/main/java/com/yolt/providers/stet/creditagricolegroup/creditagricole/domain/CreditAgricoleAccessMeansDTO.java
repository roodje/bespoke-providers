package com.yolt.providers.stet.creditagricolegroup.creditagricole.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditAgricoleAccessMeansDTO {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("id_token")
    private String idToken;

    @JsonProperty("last_sca_date")
    private String lastScaDate;

    /**
     * The region chosen when creating access means.
     * This is also needed when refreshing access means.
     */
    private CreditAgricoleRegion region;
    private String scope;
    private boolean refreshed;
}
