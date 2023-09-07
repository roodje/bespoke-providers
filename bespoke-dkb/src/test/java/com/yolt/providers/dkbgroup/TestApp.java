package com.yolt.providers.dkbgroup;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.yolt.providers.common.cryptography.JwsSigningResult;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.jose4j.jws.JsonWebSignature;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Base64;
import java.util.UUID;

@SpringBootApplication
public class TestApp {

    private static final String NOT_USED_IN_TEST_MESSAGE = "Not used in tests";

    @Bean
    MeterRegistry mockedMeterRegistry() {
        return new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
    }

    @Bean
    Clock testClock() {
        return Clock.systemUTC();
    }

    @Bean
    ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        objectMapper.setDateFormat(dateFormat);
        return objectMapper;
    }

    @Bean
    WireMockConfigurationCustomizer optionsCustomizer() {
        return options -> options
                .extensions(new ResponseTemplateTransformer(false))
                .keystoreType("pkcs12")
                .keystorePath("src/test/resources/certificates/fake-keystore.p12")
                .keystorePassword("changeit")
                .keyManagerPassword("changeit");
    }

    @Bean
    Signer signerMock() {
        return new Signer() {
            @Override
            public JwsSigningResult sign(JsonWebSignature jws, UUID privateKid, SignatureAlgorithm signatureAlgorithm) {
                throw new UnsupportedOperationException(NOT_USED_IN_TEST_MESSAGE);
            }

            @Override
            public String sign(byte[] bytesToSign, UUID privateKid, SignatureAlgorithm signatureAlgorithm) {
                return Base64.getEncoder().encodeToString(bytesToSign);
            }
        };
    }
}

