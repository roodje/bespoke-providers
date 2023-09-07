package com.yolt.providers.abnamrogroup.common.pis.pec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AbnAmroAuthorizationHttpHeadersProviderTest {

    @InjectMocks
    private AbnAmroAuthorizationHttpHeadersProvider subject;

    @Test
    void shouldReturnHttpHeadersForProvideHttpHeadersForPisTokenWhenCorrectData() {
        // when
        HttpHeaders result = subject.provideHttpHeadersForPisToken();

        // then
        assertThat(result.toSingleValueMap()).containsExactlyInAnyOrderEntriesOf(Map.of(
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE
        ));
    }
}