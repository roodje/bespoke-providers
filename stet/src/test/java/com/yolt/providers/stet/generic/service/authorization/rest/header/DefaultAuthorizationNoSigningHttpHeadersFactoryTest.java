package com.yolt.providers.stet.generic.service.authorization.rest.header;

import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultAuthorizationNoSigningHttpHeadersFactoryTest {

    private final DefaultAuthorizationNoSigningHttpHeadersFactory sut = new DefaultAuthorizationNoSigningHttpHeadersFactory();

    @Mock
    private DefaultAuthenticationMeans authMeans;

    @Test
    void shouldReturnProperSetOfHttpHeadersForCreateAccessTokenHeaders() {
        // given
        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        expectedHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // when
        HttpHeaders result = sut.createAccessTokenHeaders(authMeans, null, null);

        // then
        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedHeaders);
    }

}
