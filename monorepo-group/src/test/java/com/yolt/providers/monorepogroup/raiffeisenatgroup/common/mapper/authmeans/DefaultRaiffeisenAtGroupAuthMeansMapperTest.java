package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans;


import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.fixtures.RaiffeisenGroupAtAuthenticationMeansFixture;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans.CLIENT_ID_NAME;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
class DefaultRaiffeisenAtGroupAuthMeansMapperTest {

    DefaultRaiffeisenAtGroupAuthMeansMapper authMeansMapper = new DefaultRaiffeisenAtGroupAuthMeansMapper();

    @Test
    void shouldReturnCorrectlyMappedRaiffeisenAtGroupAuthenticationMeans() {
        //given
        var transportKeyId = UUID.randomUUID();
        var clientId = UUID.randomUUID().toString();
        var authenticationMeans = RaiffeisenGroupAtAuthenticationMeansFixture.getAuthMeansMap(transportKeyId.toString(), clientId.toString());
        var expectedAuthenticationMeans = new RaiffeisenAtGroupAuthenticationMeans(transportKeyId, null, clientId);

        //when
        var mappedAuthenticationMeans = authMeansMapper.map(authenticationMeans, "PROVIDER_KEY");

        //then
        assertThat(mappedAuthenticationMeans).usingRecursiveComparison().ignoringFields("transportCertificate")
                .isEqualTo(expectedAuthenticationMeans);
    }

    @Test
    void shouldThrowMissingAuthenticationMeansExceptionWhenOneOfAuthMeansIsMissing() {
        //given
        var transportKeyId = UUID.randomUUID();
        var authenticationMeans = RaiffeisenGroupAtAuthenticationMeansFixture.getAuthMeansMap(transportKeyId.toString(), null);
        authenticationMeans.remove(CLIENT_ID_NAME);

        //when
        ThrowableAssert.ThrowingCallable call = () -> authMeansMapper.map(authenticationMeans, "PROVIDER_KEY");

        //then
        assertThatExceptionOfType(MissingAuthenticationMeansException.class)
                .isThrownBy(call)
                .withMessage("Missing authentication mean for PROVIDER_KEY, authenticationKey=client-id");
    }

    @Test
    void shouldReturnCorrectlyMappedRaiffeisenAtGroupAuthenticationMeansForAutoonboarding() {
        //given
        var transportKeyId = UUID.randomUUID();
        var clientId = UUID.randomUUID().toString();
        var authenticationMeans = RaiffeisenGroupAtAuthenticationMeansFixture.getAuthMeansMap(transportKeyId.toString(), clientId.toString());
        var expectedAuthenticationMeans = new RaiffeisenAtGroupAuthenticationMeans(transportKeyId, null, null);

        //when
        var mappedAuthenticationMeans = authMeansMapper.mapForAutoonboarding(authenticationMeans, "PROVIDER_KEY");

        //then
        assertThat(mappedAuthenticationMeans.getTransportCertificate()).isNotNull();
        assertThat(expectedAuthenticationMeans).usingRecursiveComparison().ignoringFields("transportCertificate")
                .isEqualTo(mappedAuthenticationMeans);
    }

}