package com.yolt.providers.openbanking.ais;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.JettySettings;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.HttpServer;
import com.github.tomakehurst.wiremock.http.HttpServerFactory;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.jetty9.JettyHttpServer;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.common.rest.YoltProxySelectorBuilder;
import com.yolt.providers.openbanking.ais.utils.JwsSignatureHeaderExtractingStubRequestFilter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import wiremock.org.eclipse.jetty.io.NetworkTrafficListener;
import wiremock.org.eclipse.jetty.server.ConnectionFactory;
import wiremock.org.eclipse.jetty.server.ServerConnector;
import wiremock.org.eclipse.jetty.server.SslConnectionFactory;

import java.security.Security;
import java.time.Clock;

/**
 * Test configuration. Solely to make some integrationstest 'easier to make'.
 * This is created when splitting of the providers. A lot of providers made use of spring of the application where they were actually used
 * to do some autowiring in integrationtests.
 */
@Configuration
public class TestConfiguration {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    MeterRegistry mockedMeterRegistry() {
        return new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
    }

    @Bean
    Clock testClock() {
        return Clock.systemUTC();
    }

    @Bean
    YoltProxySelectorBuilder yoltProxySelector() {
        return new YoltProxySelectorBuilder("Dummy", 0);
    }

    @Bean
    ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory() {
        ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory = new ExternalRestTemplateBuilderFactory();
        externalRestTemplateBuilderFactory.requestFactory(SimpleClientHttpRequestFactory::new);
        return externalRestTemplateBuilderFactory;
    }

    @Bean
    WireMockConfigurationCustomizer optionsCustomizer() {
        return options -> options
                .extensions(new ResponseTemplateTransformer(false), new JwsSignatureHeaderExtractingStubRequestFilter())
                .keystoreType("pkcs12")
                .keystorePath("src/test/resources/certificates/fake/fake-keystore.p12")
                .keystorePassword("changeit")
                .keyManagerPassword("changeit");
    }

    @Bean
    public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter(@Qualifier("OpenBanking") ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }

    /**
     * Needed for SSL handshake in combination with pkcs12 keystore.
     */
    private class Pkcs12FriendlyHttpsServerFactory implements HttpServerFactory {
        @Override
        public HttpServer buildHttpServer(Options options, AdminRequestHandler adminRequestHandler, StubRequestHandler stubRequestHandler) {
            return new JettyHttpServer(
                    options,
                    adminRequestHandler,
                    stubRequestHandler
            ) {
                @Override
                protected ServerConnector createServerConnector(String bindAddress, JettySettings jettySettings, int port, NetworkTrafficListener listener, ConnectionFactory... connectionFactories) {
                    if (port == options.httpsSettings().port()) {
                        SslConnectionFactory sslConnectionFactory = (SslConnectionFactory) connectionFactories[0];
                        sslConnectionFactory.getSslContextFactory().setKeyStorePassword(options.httpsSettings().keyStorePassword());
                        connectionFactories = new ConnectionFactory[]{sslConnectionFactory, connectionFactories[1]};
                    }
                    return super.createServerConnector(bindAddress, jettySettings, port, listener, connectionFactories);
                }
            };

        }
    }
}
