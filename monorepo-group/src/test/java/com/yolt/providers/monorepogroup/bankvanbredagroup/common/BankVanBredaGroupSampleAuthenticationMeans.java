package com.yolt.providers.monorepogroup.bankvanbredagroup.common;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.KEY_ID;
import static com.yolt.providers.monorepogroup.bankvanbredagroup.BankVanBredaGroupAuthenticationMeans.*;

@Getter
public class BankVanBredaGroupSampleAuthenticationMeans {

    public static final String TPP_ID_SAMPLE = "PSDNL-SBX-1234512345";
    public static final UUID TRANSPORT_KEY_ID_SAMPLE = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public Map<String, BasicAuthenticationMean> authenticationMeans;

    public BankVanBredaGroupSampleAuthenticationMeans() throws IOException, URISyntaxException {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(TPP_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.TPP_ID.getType(), TPP_ID_SAMPLE));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadPemFile("fake-certificate.pem")));
        authenticationMeans.put(TRANSPORT_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID_SAMPLE.toString()));
    }

    private String loadPemFile(final String fileName) throws IOException, URISyntaxException {
        URI uri = BankVanBredaGroupSampleAuthenticationMeans.class
                .getClassLoader()
                .getResource("certificates/" + fileName)
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}