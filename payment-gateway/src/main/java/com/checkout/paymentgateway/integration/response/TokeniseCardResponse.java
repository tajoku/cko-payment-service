package com.checkout.paymentgateway.integration.response;

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
