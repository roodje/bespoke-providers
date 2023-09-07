package com.yolt.providers.axabanque.common;

import com.yolt.providers.common.providerinterface.Provider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class Context {

    @Autowired
    @Qualifier("ComdirectDataProviderV1")
    private UrlDataProvider comdirectDataProvider;

    private Stream<Arguments> getAllAxaDataProvidersWithMetadata() {
        return Stream.of(
                arguments(comdirectDataProvider, "COMDIRECT", ProviderVersion.VERSION_1)
        );
    }

    @Test
    void isContextLoading() {

    }

    @ParameterizedTest
    @MethodSource("getAllAxaDataProvidersWithMetadata")
    void shouldReturnProviderVersionAndIdentifier(Provider provider,
                                                  String expectedProviderIdentifier,
                                                  ProviderVersion expectedProviderVersion) {
        //when
        ProviderVersion version = provider.getVersion();
        String identifier = provider.getProviderIdentifier();
        //then
        assertThat(version).isEqualTo(expectedProviderVersion);
        assertThat(identifier).isEqualTo(expectedProviderIdentifier);
    }
}
