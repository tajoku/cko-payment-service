package com.checkout.paymentgateway.api.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CardDetailsResponse {

    @NonNull
    private String nameOnCard;

    @NonNull
    private String maskedCardNumber;

    @NonNull
    private String cardToken;

    @NonNull
    private Integer expiryMonth;

    @NonNull
    private Integer expiryYear;
}
