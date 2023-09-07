package com.yolt.providers.openbanking.ais.danske.ais.v7;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.danske.DanskeBankDataProviderV7;
import com.yolt.providers.openbanking.ais.danske.oauth2.DanskeAuthMeansBuilderV3;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_7;
import static com.yolt.providers.openbanking.ais.danske.oauth2.DanskeAuthMeansBuilderV3.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DanskeBankDataProviderTest {

    private DanskeBankDataProviderV7 provider;

    @BeforeEach
    public void setup() {
        provider = new DanskeBankDataProviderV7(null,
                null,
                null,
                null,
                null,
                new ProviderIdentification("DANSKEBANK", "Danske Bank", ProviderVersion.VERSION_7),
                typedAuthMeans -> DanskeAuthMeansBuilderV3.createDefaultAuthenticationMeans(typedAuthMeans, "DANSKE"),
                getTypedAuthenticationMeansForAIS(),
                null,
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                null,
                null);
    }

    @Test
    public void shouldReturnVersion() {
        // when
        ProviderVersion version = provider.getVersion();
        // then
        assertThat(version).isEqualTo(VERSION_7);
    }

    @Test
    public void shouldReturnIdentifier() {
        // when
        String identifier = provider.getProviderIdentifier();
        // then
        assertThat(identifier).isEqualTo("DANSKEBANK");
    }

    @Test
    public void shouldReturnDisplayName() {
        // when
        String displayName = provider.getProviderIdentifierDisplayName();
        // then
        assertThat(displayName).isEqualTo("Danske Bank");
    }

    @Test
    public void shouldReturnTransportKeyRequirements() {
        // when
        KeyRequirements transportKeyRequirements = provider.getTransportKeyRequirements().get();
        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME).get());
    }

    @Test
    public void shouldReturnSigningKeyRequirements() {
        // when
        KeyRequirements signingKeyErquirements = provider.getSigningKeyRequirements().get();
        // then
        assertThat(signingKeyErquirements).isEqualTo(HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME).get());
    }

    @Test
    public void shouldReturnTypedAuthenticationMeans() {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = provider.getTypedAuthenticationMeans();
        // then
        assertThat(authenticationMeans).containsOnlyKeys(
                INSTITUTION_ID_NAME,
                CLIENT_ID_NAME,
                SIGNING_KEY_HEADER_ID_NAME,
                SIGNING_PRIVATE_KEY_ID_NAME,
                TRANSPORT_CERTIFICATE_NAME,
                TRANSPORT_PRIVATE_KEY_ID_NAME,
                SOFTWARE_ID_NAME,
                SOFTWARE_STATEMENT_ASSERTION_NAME);
    }
}
