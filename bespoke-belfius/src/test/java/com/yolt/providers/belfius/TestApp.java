package com.yolt.providers.belfius;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.yolt.providers.common.cryptography.JwsSigningResult;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.jose4j.jws.JsonWebSignature;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Function;

@SpringBootApplication
public class TestApp {

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
                throw new UnsupportedOperationException("Not used in tests");
            }

            @Override
            public String sign(byte[] bytesToSign, UUID privateKid, SignatureAlgorithm signatureAlgorithm) {
                return Base64.getEncoder().encodeToString(bytesToSign);
            }
        };
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    RestTemplateManager restTemplateManagerMock() {
        ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory = new ExternalRestTemplateBuilderFactory();
        externalRestTemplateBuilderFactory.requestFactory(SimpleClientHttpRequestFactory::new);

        return new RestTemplateManager() {
            @Override
            public RestTemplate manage(UUID privateTransportKid, X509Certificate[] clientCertificateChain, Function<ExternalRestTemplateBuilderFactory, RestTemplate> customizationFunction) {
                return customizationFunction.apply(externalRestTemplateBuilderFactory);
            }

            @Override
            public RestTemplate manage(RestTemplateManagerConfiguration restTemplateManagerConfiguration) {
                return restTemplateManagerConfiguration.getCustomizationFunction().apply(externalRestTemplateBuilderFactory);
            }
        };
    }
}
