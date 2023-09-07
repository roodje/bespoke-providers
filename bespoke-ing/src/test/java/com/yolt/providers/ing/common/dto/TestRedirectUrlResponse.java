package com.yolt.providers.ing.common.dto;

import com.yolt.providers.ing.common.auth.RedirectUrlResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class TestRedirectUrlResponse implements RedirectUrlResponse {

    private final String location;
}
