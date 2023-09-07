package com.yolt.providers.openbanking.ais.generic2.domain;

import lombok.Value;

import java.util.List;

@Value
public class AccessMeansState<T extends AccessMeans> {
    T accessMeans;
    List<String> permissions;
}
