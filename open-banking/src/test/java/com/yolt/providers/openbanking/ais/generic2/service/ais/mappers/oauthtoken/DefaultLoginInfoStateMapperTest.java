package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.domain.LoginInfoState;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultLoginInfoStateMapperTest {
    private final ObjectMapper objectMapper = OpenBankingTestObjectMapper.INSTANCE;
    private final LoginInfoStateMapper<LoginInfoState> loginInfoStateMapper = new DefaultLoginInfoStateMapper<>(objectMapper);

    @Test
    public void shouldMapLoginInfoStateToJson() {
        //given
        String userId = "11100000-1111-1111-1111-10aaa1aa00a";
        LoginInfoState loginInfoState = new LoginInfoState(List.of("ReadParty"));
        //when
        String json = loginInfoStateMapper.toJson(loginInfoState);
        //then
        assertThat(json).isEqualTo("""
                {"permissions":["ReadParty"]}\
                """);
    }

    @Test
    public void shouldMapLoginInfoFromJson() throws TokenInvalidException {
        //given
        String json = """
                {"permissions":["ReadParty"]}\
                """;
        //when
        LoginInfoState state = loginInfoStateMapper.fromJson(json);
        //then
        assertThat(state).isEqualTo(new LoginInfoState(List.of("ReadParty")));
    }
}
