package com.yolt.providers.fabric;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.types.NoWhiteCharacterStringType;
import com.yolt.providers.fabric.common.FabricGroupDataProviderV1;
import lombok.SneakyThrows;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.yolt.providers.common.domain.authenticationmeans.RenderingType.ONE_LINE_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.fabric.common.auth.FabricGroupAuthenticationMeans.*;

public class SampleAuthenticationMeans {

    private static final String CERTIFICATES_PATH = "certificates/fake-certificate.pem";
    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    static {
        String certificateString = readFakeCertificatePem();
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_SIGNING_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "0677504b-4c38-4c77-a50e-e979205f63ec"));
        authenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), "84413cd6-de73-4c55-9413-a730a68d2a55"));
        authenticationMeans.put(CLIENT_SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CLIENT_SIGNING_CERTIFICATE_PEM.getType(), certificateString));
        authenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATES_CHAIN_PEM.getType(), certificateString));
        authenticationMeans.put(ONBOARDING_URL, new BasicAuthenticationMean(new TypedAuthenticationMeans("Onboarding url", NoWhiteCharacterStringType.getInstance(), ONE_LINE_STRING).getType(), "onboarding/25385a3e92246d3?token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJmYWJyaWNrIiwiZGF0YSI6eyJndWlkIjoiMjUzODVhM2U5MjI0NmQzIn19.Z39F4vI_cjRMt3iy4wgpAl1WowNZNKjk9lXPIBGqxAAzT1Chy2mMxbjRYZ6nz_OoGgNQjnxHP9BPc7Pr85ds5A"));
    }

    public static Map<String, BasicAuthenticationMean> getSampleAuthenticationMeans() {
        return authenticationMeans;
    }

    @SneakyThrows
    public static String readFakeCertificatePem() {
        URL certificateUrl = FabricGroupDataProviderV1.class.getClassLoader().getResource(CERTIFICATES_PATH);
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
    }
}
