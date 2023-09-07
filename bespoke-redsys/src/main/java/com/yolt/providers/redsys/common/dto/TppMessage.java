package com.yolt.providers.redsys.common.dto;

import lombok.Data;

@Data
public class TppMessage {
    private String category;

    private String code;

    private String text;

    private String path;
}
