package com.yolt.providers.yoltprovider;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.types.PemType;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import com.yolt.providers.common.domain.authenticationmeans.types.UuidType;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@UtilityClass
public class TestObjects {
    @SneakyThrows
    public static Map<String, BasicAuthenticationMean> createAuthenticationMeans() {
        URI fileURI = YoltProviderV2IntegrationTest.class
                .getClassLoader()
                .getResource("test_certificate.pem")
                .toURI();
        String certificate = String.join("\n", Files.readAllLines(new File(fileURI).toPath(), StandardCharsets.UTF_8));
        return new HashMap<>() {{
            put("client-id", new BasicAuthenticationMean(StringType.getInstance(), "someId"));
            put("client-secret", new BasicAuthenticationMean(StringType.getInstance(), "someId"));
            put("client-signing-private-key-id", new BasicAuthenticationMean(UuidType.getInstance(), UUID.randomUUID().toString()));
            put("client-transport-private-key-id", new BasicAuthenticationMean(UuidType.getInstance(), UUID.randomUUID().toString()));
            put("client-signing-certificate", new BasicAuthenticationMean(PemType.getInstance(), certificate));
            put("client-transport-certificate", new BasicAuthenticationMean(PemType.getInstance(), certificate));
        }};
    }
}
