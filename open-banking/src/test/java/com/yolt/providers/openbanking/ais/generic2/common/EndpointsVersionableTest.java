package com.yolt.providers.openbanking.ais.generic2.common;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EndpointsVersionableTest {

    private EndpointsVersionable notConfiguredEndpointsVersionable = () -> "";
    private EndpointsVersionable configuredEndpointsVersionable = () -> "/v1.0";

    @Test
    public void shouldReturnSamePathForGetAdjustedUrlPathWhenVersionForEndpointsNotConfigured() {
        // Given
        String urlPath = "/ais/accounts";

        // When
        String urlPathWithVersion = notConfiguredEndpointsVersionable.getAdjustedUrlPath(urlPath);

        // Then
        assertThat(urlPathWithVersion).isEqualTo(urlPath);
    }

    @Test
    public void shouldReturnSamePathForGetAdjustedUrlPathWhenVersionForEndpointsIsConfiguredAndAbsolutePathIsProvided() {
        // Given
        String urlPath = "https://test.com/ais/accounts";

        // When
        String urlPathWithVersion = configuredEndpointsVersionable.getAdjustedUrlPath(urlPath);

        // Then
        assertThat(urlPathWithVersion).isEqualTo(urlPath);
    }

    @Test
    public void shouldReturnSamePathForGetAdjustedUrlPathWhenVersionForEndpointsIsConfiguredAndNullIsProvided() {
        // Given
        String urlPath = null;

        // When
        String urlPathWithVersion = configuredEndpointsVersionable.getAdjustedUrlPath(urlPath);

        // Then
        assertThat(urlPathWithVersion).isEqualTo(urlPath);
    }

    @Test
    public void shouldReturnSamePathForGetAdjustedUrlPathWhenVersionForEndpointsIsConfiguredAndEmptyPathIsProvided() {
        // Given
        String urlPath = "";

        // When
        String urlPathWithVersion = configuredEndpointsVersionable.getAdjustedUrlPath(urlPath);

        // Then
        assertThat(urlPathWithVersion).isEqualTo(urlPath);
    }

    @Test
    public void shouldReturnSamePathForGetAdjustedUrlPathWhenVersionForEndpointsIsConfiguredAndRelativePathStartsWithSameVersion() {
        // Given
        String urlPath = "/v1.0/ais/accounts";

        // When
        String urlPathWithVersion = configuredEndpointsVersionable.getAdjustedUrlPath(urlPath);

        // Then
        assertThat(urlPathWithVersion).isEqualTo(urlPath);
    }

    @Test
    public void shouldReturnAdjustedPathWithVersionForGetAdjustedUrlPathWhenVersionForEndpointsIsConfiguredAndRelativePathIsProvided() {
        // Given
        String urlPath = "/ais/accounts";

        // When
        String urlPathWithVersion = configuredEndpointsVersionable.getAdjustedUrlPath(urlPath);

        // Then
        assertThat(urlPathWithVersion).isEqualTo("/v1.0" + urlPath);
    }
}
