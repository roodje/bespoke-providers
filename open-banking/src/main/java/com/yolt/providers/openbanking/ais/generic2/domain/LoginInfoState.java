package com.yolt.providers.openbanking.ais.generic2.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginInfoState {
    List<String> permissions;
}
