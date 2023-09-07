package com.yolt.providers.stet.generic.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataProviderState {

    private Region region;
    private String codeVerifier;
    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private boolean refreshed = false;

    public static DataProviderState emptyState() {
        return new DataProviderState();
    }

    public static DataProviderState preAuthorizedProviderState(Region region, AuthorizationRedirect authorizationRedirect) {
        return new DataProviderState(region, authorizationRedirect != null ? authorizationRedirect.getProofKeyCodeExchangeCodeVerifier() : null);
    }

    public static DataProviderState authorizedProviderState(Region region, String accessToken) {
        return authorizedProviderState(region, accessToken, null);
    }

    public static DataProviderState authorizedProviderState(Region region, String accessToken, String refreshToken) {
        return authorizedProviderState(region, accessToken, refreshToken, false);
    }

    public static DataProviderState authorizedProviderState(Region region, String accessToken, String refreshToken, boolean refreshed) {
        return new DataProviderState(region, accessToken, refreshToken, refreshed);
    }

    private DataProviderState(Region region, String codeVerifier) {
        this.region = region;
        this.codeVerifier = codeVerifier;
    }

    private DataProviderState(Region region, String accessToken, String refreshToken, boolean refreshed) {
        this.region = region;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.refreshed = refreshed;
    }

    @JsonIgnore
    public boolean hasRegion() {
        return Objects.nonNull(region);
    }
}
