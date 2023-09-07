package com.yolt.providers.amexgroup.common;

import com.yolt.providers.amexgroup.common.auth.AmexGroupAuthMeanProducerV6;
import com.yolt.providers.amexgroup.common.auth.AmexGroupAuthMeans;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.util.KeyUtil;
import org.junit.jupiter.api.Test;

import java.security.cert.CertificateException;
import java.util.Map;

import static com.yolt.providers.amexgroup.AmexGroupDataProviderIntegrationTest.prepareMeans;
import static com.yolt.providers.amexgroup.common.utils.AmexAuthMeansFields.*;
import static org.assertj.core.api.Assertions.assertThat;

public class AmexAuthMeansProducerV6Test {

    private static final Map<String, BasicAuthenticationMean> AUTH_MEANS = prepareMeans();

    @Test
    public void shouldProduceProperAuthMeans() throws CertificateException {
        // given
        AmexGroupAuthMeanProducerV6 amexGroupAuthMeanProducerV6 = new AmexGroupAuthMeanProducerV6();

        // when
        AmexGroupAuthMeans amexGroupAuthMeans = amexGroupAuthMeanProducerV6.createAuthMeans(AUTH_MEANS, "AMEX");

        // then
        assertThat(amexGroupAuthMeans.getClientId()).isEqualTo(AUTH_MEANS.get(CLIENT_ID).getValue());
        assertThat(amexGroupAuthMeans.getClientSecret()).isEqualTo(AUTH_MEANS.get(CLIENT_SECRET).getValue());
        assertThat(amexGroupAuthMeans.getTransportKeyId().toString()).isEqualTo(AUTH_MEANS.get(CLIENT_TRANSPORT_KEY_ID_ROTATION).getValue());
        assertThat(amexGroupAuthMeans.getClientTransportCertificate()).isEqualTo(KeyUtil.createCertificateFromPemFormat(AUTH_MEANS.get(CLIENT_TRANSPORT_CERTIFICATE_ROTATION).getValue()));
    }
}



