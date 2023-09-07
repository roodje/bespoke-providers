package com.yolt.providers.nutmeggroup.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PotsResponse {
    private List<Pot> pots;
}
