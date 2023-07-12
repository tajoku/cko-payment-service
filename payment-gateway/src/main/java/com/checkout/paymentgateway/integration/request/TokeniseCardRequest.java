package com.checkout.paymentgateway.integration.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TokeniseCardRequest {

    @NonNull
    private String nameOnCard;

    @NonNull
    private String cardNumber;

    @NonNull
    private String cvv;

    @NonNull
    private Integer expiryMonth;

    @NonNull
    private Integer expiryYear;

}
