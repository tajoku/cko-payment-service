package com.checkout.paymentgateway.api.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CardDetailsRequest {

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
