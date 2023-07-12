package com.checkout.cko.api.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TokeniseCardResponse {
    private String token;

    private String maskedCardNumber;
}
