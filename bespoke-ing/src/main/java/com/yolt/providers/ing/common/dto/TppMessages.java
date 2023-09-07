package com.yolt.providers.ing.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TppMessages {

    private String category;
    private String code;
    private String text;
    private String path;
}
