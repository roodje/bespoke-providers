package com.yolt.providers.direkt1822group.common.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Balance {

    private Amount balanceAmount;
    private String balanceType;
    private String lastChangeDateTime;

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public enum Type {
        CLOSING_BOOKED("closingBooked"),
        AUTHORISED("authorised");

        private final String name;
    }
}
